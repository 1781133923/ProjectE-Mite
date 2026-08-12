package moze_intel.projecte.mixins;

import java.util.HashMap;
import java.util.Map;

import moze_intel.projecte.gameObjs.items.armor.GemArmorBase;
import net.minecraft.EntityPlayer;
import net.minecraft.InventoryPlayer;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gem armour pieces cannot be stripped by external mechanics: the
 * cannot_wear_armor curse (InventoryPlayer.dropAllArmor) and death drops
 * (InventoryPlayer.dropAllItems) skip gem pieces and leave them equipped.
 * Taking the pieces off manually through the inventory GUI never goes through
 * these methods, so player removal is unaffected.
 */
@Mixin(InventoryPlayer.class)
public abstract class ProjectEGemArmorLockMixin
{
	@Shadow
	public ItemStack[] armorInventory;

	@Shadow
	public EntityPlayer player;

	private static final Map<EntityPlayer, ItemStack[]> STASHED = new HashMap<EntityPlayer, ItemStack[]>();

	private void projecte$stashGemArmor()
	{
		ItemStack[] armor = this.armorInventory;
		ItemStack[] saved = new ItemStack[armor.length];
		boolean any = false;
		for (int i = 0; i < armor.length; i++)
		{
			if (armor[i] != null && armor[i].getItem() instanceof GemArmorBase)
			{
				saved[i] = armor[i];
				armor[i] = null;
				any = true;
			}
		}
		if (any)
		{
			STASHED.put(this.player, saved);
		}
	}

	private void projecte$restoreGemArmor()
	{
		ItemStack[] saved = STASHED.remove(this.player);
		if (saved != null)
		{
			for (int i = 0; i < saved.length && i < this.armorInventory.length; i++)
			{
				if (saved[i] != null)
				{
					this.armorInventory[i] = saved[i];
				}
			}
		}
	}

	@Inject(method = "dropAllArmor", at = @At("HEAD"))
	private void projecte$stashForCurse(CallbackInfoReturnable<Boolean> cir)
	{
		projecte$stashGemArmor();
	}

	@Inject(method = "dropAllArmor", at = @At("TAIL"))
	private void projecte$restoreForCurse(CallbackInfoReturnable<Boolean> cir)
	{
		projecte$restoreGemArmor();
	}

	@Inject(method = "dropAllItems", at = @At("HEAD"))
	private void projecte$stashForDeath(CallbackInfo ci)
	{
		projecte$stashGemArmor();
	}

	@Inject(method = "dropAllItems", at = @At("TAIL"))
	private void projecte$restoreForDeath(CallbackInfo ci)
	{
		projecte$restoreGemArmor();
	}
}