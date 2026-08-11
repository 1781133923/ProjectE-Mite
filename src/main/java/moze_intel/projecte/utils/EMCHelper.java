package moze_intel.projecte.utils;

import com.google.common.collect.Maps;
import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.emc.EMCMapper;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.emc.SimpleStack;
import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.IInventory;
import net.minecraft.Item;
import net.minecraft.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for EMC.
 * Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class EMCHelper
{
	/**
	 * Consumes EMC from fuel items or Klein Stars
	 * Any extra EMC is discarded !!! To retain remainder EMC use ItemPE.consumeFuel()
	 */
	public static double consumePlayerFuel(EntityPlayer player, double minFuel)
	{
		if (player.capabilities.isCreativeMode)
		{
			return minFuel;
		}

		IInventory inv = player.inventory;
		LinkedHashMap<Integer, Integer> map = Maps.newLinkedHashMap();
		boolean metRequirement = false;
		int emcConsumed = 0;

		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);

			if (stack == null)
			{
				continue;
			}
			else if (stack.getItem() instanceof IItemEmc)
			{
				IItemEmc itemEmc = ((IItemEmc) stack.getItem());
				if (itemEmc.getStoredEmc(stack) >= minFuel)
				{
					itemEmc.extractEmc(stack, minFuel);
					player.inventoryContainer.detectAndSendChanges();
					return minFuel;
				}
			}
			else if (!metRequirement)
			{
				if(FuelMapper.isStackFuel(stack))
				{
					int emc = getEmcValue(stack);
					int toRemove = ((int) Math.ceil((minFuel - emcConsumed) / (float) emc));

					if (stack.stackSize >= toRemove)
					{
						map.put(i, toRemove);
						emcConsumed += emc * toRemove;
						metRequirement = true;
					}
					else
					{
						map.put(i, stack.stackSize);
						emcConsumed += emc * stack.stackSize;

						if (emcConsumed >= minFuel)
						{
							metRequirement = true;
						}
					}

				}
			}
		}

		if (metRequirement)
		{
			for (Map.Entry<Integer, Integer> entry : map.entrySet())
			{
				inv.decrStackSize(entry.getKey(), entry.getValue());
			}

			player.inventoryContainer.detectAndSendChanges();
			return emcConsumed;
		}

		return -1;
	}

	public static boolean doesBlockHaveEmc(Block block)
	{
		if (block == null)
		{
			return false;
		}

		return doesItemHaveEmc(new ItemStack(block));
	}

	public static boolean doesItemHaveEmc(ItemStack stack)
	{
		if (stack == null)
		{
			return false;
		}

		SimpleStack iStack = new SimpleStack(stack);

		if (!iStack.isValid())
		{
			return false;
		}

		if (EMCMapper.mapContains(iStack))
		{
			return true;
		}

		// MITE damageable items that also have subtypes (e.g. metal anvils
		// whose subtype is the wear stage) only have EMC on their subtype-0
		// entry; fall back to it so a worn anvil can still be recycled.
		if (stack.getItem() != null && stack.getItem().isDamageable())
		{
			iStack.damage = 0;
			return EMCMapper.mapContains(iStack);
		}

		return false;
	}

	public static boolean doesItemHaveEmc(Item item)
	{
		if (item == null)
		{
			return false;
		}

		return doesItemHaveEmc(new ItemStack(item));
	}

	public static int getEmcValue(Block Block)
	{
		SimpleStack stack = new SimpleStack(new ItemStack(Block));

		if (stack.isValid() && EMCMapper.mapContains(stack))
		{
			return EMCMapper.getEmcValue(stack);
		}

		return 0;
	}

	public static int getEmcValue(Item item)
	{
		SimpleStack stack = new SimpleStack(new ItemStack(item));

		if (stack.isValid() && EMCMapper.mapContains(stack))
		{
			return EMCMapper.getEmcValue(stack);
		}

		return 0;
	}

	/**
	 * Does not consider stack size
	 */
	public static int getEmcValue(ItemStack stack)
	{
		if (stack == null)
		{
			return 0;
		}

		SimpleStack iStack = new SimpleStack(stack);

		if (!iStack.isValid())
		{
			return 0;
		}

		if (stack.isItemStackDamageable() && !isUnbreakableProjectEItem(stack))
		{
			// MITE stores item variants in the subtype field and durability
			// loss in getItemDamage(). The base (total) EMC is always the
			// intact subtype-0 entry; the subtype of a worn anvil is just its
			// wear stage and must not influence the price. Apply the
			// remaining-durability scaling for every damageable item:
			// value = total EMC x remaining / max. An intact item has ratio 1
			// (value unchanged), so damaged tools, anvils and armour can no
			// longer be sold for their full price and re-bought to dodge
			// repair costs.
			iStack.damage = 0;
			if (!EMCMapper.mapContains(iStack))
			{
				return 0;
			}
			int emc = EMCMapper.getEmcValue(iStack);

			int relDamage = (stack.getMaxDamage() - stack.getItemDamage());

			if (relDamage <= 0)
			{
				//Not Impossible. Don't use durability or enchants for emc calculation if this happens.
				return emc;
			}

			// emc and relDamage are ints; multiply in long space or large
			// durabilities (metal anvils reach millions x millions) overflow
			// int and silently wrap to a wrong positive value.
			long result = (long) emc * relDamage;

			if (result <= 0)
			{
				//Congratulations, big number is big.
				return emc;
			}

			result /= stack.getMaxDamage();

			result += getStoredEMCBonus(stack);

			if (result > Integer.MAX_VALUE)
			{
				return emc;
			}

			if (result <= 0)
			{
				return 1;
			}

			return (int) result;
		}
		else
		{
			if (EMCMapper.mapContains(iStack))
			{
				return EMCMapper.getEmcValue(iStack) + (int)getStoredEMCBonus(stack);
			}
		}

		return 0;
	}

	/**
	 * EMC granted when SELLING an item (transmutation consume slot,
	 * condensers): the item's EMC divided by the configured buy/sell ratio.
	 * Ratio 8 means selling gives 1/8 of the EMC.
	 */
	public static int getSellValue(ItemStack stack)
	{
		int emc = getEmcValue(stack);
		float ratio = moze_intel.projecte.config.ProjectEOConfig.emcExchangeRatio;
		if (ratio <= 1.0F || emc <= 0)
		{
			return emc;
		}
		return (int) (emc / ratio);
	}

	/**
	 * EMC charged when BUYING an item (transmutation outputs, condenser
	 * locks): the item's EMC multiplied by the configured buy/sell ratio.
	 * Ratio 8 means buying costs 8x the EMC.
	 */
	public static int getBuyValue(ItemStack stack)
	{
		int emc = getEmcValue(stack);
		float ratio = moze_intel.projecte.config.ProjectEOConfig.emcExchangeRatio;
		if (ratio <= 1.0F || emc <= 0)
		{
			return emc;
		}
		return (int) (emc * ratio);
	}
	/**
	 * A copy of the stack with any durability loss removed. The transmutation
	 * table hands out the fully-repaired item, so purchases must be priced
	 * (and the output given) at full durability instead of the worn value.
	 */
	public static ItemStack getUndamagedCopy(ItemStack stack)
	{
		if (stack == null)
		{
			return null;
		}
		ItemStack copy = stack.copy();
		if (copy.isItemStackDamageable() && copy.getItemDamage() != 0 && !isUnbreakableProjectEItem(copy))
		{
			copy.setItemDamage(0);
		}
		return copy;
	}

	/**
	 * Dark/red matter tools reuse MITE's durability bar as the charge bar
	 * (the damage field is charge, not real wear) and never take durability
	 * damage; dark/red/gem armour is unbreakable too. Such items must always
	 * sell at their full EMC value instead of being discounted like worn
	 * tools/anvils.
	 */
	private static boolean isUnbreakableProjectEItem(ItemStack stack)
	{
		Item item = stack.getItem();
		return item instanceof moze_intel.projecte.gameObjs.items.tools.PEToolBase
				|| item instanceof moze_intel.projecte.gameObjs.items.armor.DMArmor
				|| item instanceof moze_intel.projecte.gameObjs.items.armor.RMArmor
				|| item instanceof moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
	}

	public static int getKleinStarMaxEmc(ItemStack stack)
	{
		// MITE stores item variants in getItemSubtype(); getItemDamage() is the
		// durability field and is always 0 for Klein Stars, which would cap every
		// tier at the tier-1 capacity.
		int tier = stack.getItemSubtype();
		if (tier < 0 || tier >= Constants.MAX_KLEIN_EMC.length)
		{
			tier = 0;
		}
		return Constants.MAX_KLEIN_EMC[tier];
	}

	public static double getStoredEMCBonus(ItemStack stack) {
		if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("StoredEMC")) {
			return stack.stackTagCompound.getDouble("StoredEMC");
		}
		return 0;
	}
}
