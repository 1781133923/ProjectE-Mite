package moze_intel.projecte.compat;

import moddedmite.rustedironcore.api.event.listener.ICombatListener;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.items.armor.DMArmor;
import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import moze_intel.projecte.gameObjs.items.armor.RMArmor;
import net.minecraft.Damage;
import net.minecraft.DamageSource;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.Item;
import net.minecraft.ItemStack;

/**
 * MITE does not use Forge's ISpecialArmor, so the dark/red matter armour's
 * special protections are applied through RustedIronCore's combat events:
 *  - wearing any dark/red matter piece greatly reduces explosion damage
 *  - wearing the dark/red matter helmet negates fall damage
 */
public class MITECombatListener implements ICombatListener
{
	@Override
	public void onPlayerReceiveDamageModify(EntityPlayer player, Damage damage)
	{
		DamageSource source = damage.getSource();
		if (source == null)
		{
			return;
		}

		// Full matter set bonuses: magic resistance (dark 40% / red 55% /
		// gem 75%) and full-set divine-lightning immunity for the gem set
		// (MITE's divine lightning is an absolute damage source; real
		// lightning strikes are handled separately in
		// ProjectEGemLightningMixin). Like every ProjectE armour effect this
		// only applies to players - a mob wearing the set still only benefits
		// from the raw armour value.
		if (source.hasMagicAspect())
		{
			float factor = 1.0F;
			if (GemArmorBase.hasFullSet(player))
			{
				factor = 0.25F; // 75% magic resistance
			}
			else if (RMArmor.hasFullSet(player))
			{
				factor = 0.45F; // 55%
			}
			else if (DMArmor.hasFullSet(player))
			{
				factor = 0.6F; // 40%
			}
			if (factor < 1.0F)
			{
				damage.setAmount(damage.getAmount() * factor);
			}
		}
		if (GemArmorBase.hasFullSet(player) && source == DamageSource.divine_lightning)
		{
			damage.setAmount(0.0F);
		}

		if (source.isExplosion())
		{
			float reduction = getExplosionReduction(player);
			if (reduction > 0.0F)
			{
				damage.setAmount(damage.getAmount() * (1.0F - reduction));
			}
		}
	}

	@Override
	public float onEntityLivingFallDamageModify(EntityLivingBase entity, float fallDistance,
			net.minecraft.BlockInfo blockInfo, float fallDamage)
	{
		if (entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entity;
			if (hasAnyPiece(player, ObjHandler.dmFeet, ObjHandler.rmFeet)
					|| hasWornGemFeet(player))
			{
				return 0.0F;
			}
		}
		return fallDamage;
	}

	private static float getExplosionReduction(EntityPlayer player)
	{
		if (hasAnyPiece(player, ObjHandler.gemHelmet, ObjHandler.gemChest, ObjHandler.gemLegs, ObjHandler.gemFeet))
		{
			return 0.9F;
		}
		if (hasAnyPiece(player, ObjHandler.rmHelmet, ObjHandler.rmChest, ObjHandler.rmLegs, ObjHandler.rmFeet))
		{
			return 0.9F;
		}
		if (hasAnyPiece(player, ObjHandler.dmHelmet, ObjHandler.dmChest, ObjHandler.dmLegs, ObjHandler.dmFeet))
		{
			return 0.8F;
		}
		return 0.0F;
	}

	private static boolean hasAnyPiece(EntityPlayer player, Item... pieces)
	{
		for (ItemStack stack : player.inventory.armorInventory)
		{
			if (stack == null)
			{
				continue;
			}
			for (Item piece : pieces)
			{
				if (stack.getItem() == piece)
				{
					return true;
				}
			}
		}
		return false;
	}

	private static boolean hasWornGemFeet(EntityPlayer player)
	{
		ItemStack boots = player.getCurrentArmor(0);
		return boots != null && boots.getItem() instanceof GemArmorBase;
	}
}
