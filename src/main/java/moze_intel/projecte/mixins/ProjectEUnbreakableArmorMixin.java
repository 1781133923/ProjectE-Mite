package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.armor.DMArmor;
import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import moze_intel.projecte.gameObjs.items.armor.RMArmor;
import net.minecraft.DamageSource;
import net.minecraft.EntityLivingBase;
import net.minecraft.Item;
import net.minecraft.ItemDamageResult;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dark/red matter armour never loses durability, matching the unbreakable
 * ProjectE tools. MITE's InventoryPlayer.tryDamageArmor routes all armour
 * wear through ItemStack.tryDamageItem, so intercept it here.
 */
@Mixin(ItemStack.class)
public abstract class ProjectEUnbreakableArmorMixin
{
	@Shadow
	public abstract Item getItem();

	@Inject(method = "tryDamageItem(Lnet/minecraft/DamageSource;ILnet/minecraft/EntityLivingBase;)Lnet/minecraft/ItemDamageResult;",
			at = @At("HEAD"), cancellable = true)
	private void projecte$armorNeverDecays(DamageSource source, int amount, EntityLivingBase entity,
			CallbackInfoReturnable<ItemDamageResult> cir)
	{
		Item item = getItem();
		if (item instanceof DMArmor || item instanceof RMArmor || item instanceof GemArmorBase)
		{
			cir.setReturnValue(new ItemDamageResult());
		}
	}
}
