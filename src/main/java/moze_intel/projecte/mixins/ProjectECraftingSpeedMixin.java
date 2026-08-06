package moze_intel.projecte.mixins;

import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.TimeWatch;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import net.minecraft.ClientPlayer;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Time Watch on a dark matter pedestal: players inside the pedestal's effect
 * bounds craft 18x faster. MITE computes the crafting period client-side
 * (ClientPlayer.getCraftingPeriod), so the boost is applied there by scanning
 * for an active Time Watch pedestal near the player.
 */
@Mixin(ClientPlayer.class)
public abstract class ProjectECraftingSpeedMixin
{
	@Inject(method = "getCraftingPeriod", at = @At("RETURN"), cancellable = true)
	private void projecte$timeWatchCraftingBoost(float difficulty, CallbackInfoReturnable<Integer> cir)
	{
		if (!ProjectEConfig.enableTimeWatch)
		{
			return;
		}
		Object self = this;
		if (!(self instanceof EntityPlayer))
		{
			return;
		}
		EntityPlayer player = (EntityPlayer) self;
		if (player.worldObj == null || player.worldObj.loadedTileEntityList == null)
		{
			return;
		}
		Vec3 pos = player.worldObj.getWorldVec3Pool().getVecFromPool(player.posX, player.posY, player.posZ);
		for (Object obj : player.worldObj.loadedTileEntityList)
		{
			if (obj instanceof DMPedestalTile)
			{
				DMPedestalTile tile = (DMPedestalTile) obj;
				if (!tile.getActive())
				{
					continue;
				}
				ItemStack stack = tile.getItemStack();
				if (stack == null || !(stack.getItem() instanceof TimeWatch))
				{
					continue;
				}
				if (tile.getEffectBounds().isVecInside(pos))
				{
					cir.setReturnValue(Math.max(1, cir.getReturnValueI() / 18));
					return;
				}
			}
		}
	}
}
