package moze_intel.projecte.gameObjs.items;

import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.xiaoyu233.fml.reload.utils.IdUtil;

public abstract class ItemPE extends Item
{
	public ItemPE()
	{
		// MITE-standard ID allocation (same pattern as MoreMetals): take the
		// loader's IdUtil counter directly in the constructor instead of
		// constructing with itemID 0 and patching it later via reflection.
		// All ItemPE subclasses (tools, rings, charge items, rods, amulets...)
		// route through this constructor, so they all get their IDs here.
		super(IdUtil.getNextItemID(), (String) null, 1);
		this.setCreativeTab(ObjHandler.cTab);
	}

	/**
	 * MITE fixes the subtype count in the Item constructor, so the 1.7.10-style
	 * items cannot declare their metadata count normally. Patch the private
	 * fields reflectively so the game treats the item as having subtypes.
	 */
	protected final void setSubtypes(int count)
	{
		try
		{
			java.lang.reflect.Field num = net.minecraft.Item.class.getDeclaredField("num_subtypes");
			num.setAccessible(true);
			num.setInt(this, count);
			java.lang.reflect.Field has = net.minecraft.Item.class.getDeclaredField("has_subtypes");
			has.setAccessible(true);
			has.setBoolean(this, count > 0);
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException("Failed to set subtypes on " + getClass().getName(), e);
		}
	}

	@Override
	public Item setUnlocalizedName(String message)
	{
		return super.setUnlocalizedName("pe_" + message);
	}

	public static double getEmc(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
		
		return stack.stackTagCompound.getDouble("StoredEMC");
	}
	
	public static void setEmc(ItemStack stack, double amount)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new NBTTagCompound();
		}
		
		stack.stackTagCompound.setDouble("StoredEMC", amount);
	}
	
	public static void addEmcToStack(ItemStack stack, double amount)
	{
		setEmc(stack, getEmc(stack) + amount);
	}
	
	public static void removeEmc(ItemStack stack, double amount)
	{
		double result = getEmc(stack) - amount;
		
		if (result < 0)
		{
			result = 0;
		}
		
		setEmc(stack, result);
	}
	
	public static boolean consumeFuel(EntityPlayer player, ItemStack stack, double amount, boolean shouldRemove)
	{
		if (amount <= 0)
		{
			return true;
		}

		double current = getEmc(stack);
		
		if (current < amount)
		{
			double consume = EMCHelper.consumePlayerFuel(player, amount - current);
			
			if (consume == -1)
			{
				return false;
			}
			
			addEmcToStack(stack, consume);
		}
		
		if (shouldRemove)
		{
			removeEmc(stack, amount);
		}
		
		return true;
	}
	
	public String getTexture(String name)
	{
		return ("projecte:" + name);
	}
	
	public String getTexture(String folder, String name)
	{
		return ("projecte:" + folder + "/" + name);
	}

	/**
	 * MITE's Item.canCatchFire walks the item's materials list and prints an
	 * error for items with no materials. ProjectE items are magical and
	 * fire-proof, so short-circuit both checks (also silences the repeated
	 * "materials list is empty" console spam).
	 */
	@Override
	public boolean canCatchFire()
	{
		return false;
	}

	@Override
	public boolean isHarmedByFire()
	{
		return false;
	}
}
