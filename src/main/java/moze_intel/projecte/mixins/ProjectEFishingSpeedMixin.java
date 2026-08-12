package moze_intel.projecte.mixins;

import java.util.Random;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.TimeWatch;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import net.minecraft.EntityFishHook;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Time Watch on a dark matter pedestal: nearby players fish 18x faster.
 *
 * MITE's fishing cadence is NOT driven by ticksCatchable (it stays 0 until a
 * bite and only marks the 30-59 tick "fish hooked" reel-in window; draining it
 * shrinks that window instead of speeding up the wait). checkForBite() rolls
 * rand.nextInt(waitTime) == 0 every tick, waitTime 600-2400 ticks. Redirect
 * that last nextInt and divide the bound by 18 while the angler stands inside
 * an active Time Watch pedestal's effect bounds.
 */
@Mixin(EntityFishHook.class)
public abstract class ProjectEFishingSpeedMixin
{
	@Shadow
	public EntityPlayer angler;

	@Redirect(method = "checkForBite", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 3))
	private int projecte$timeWatchFishingBoost(Random rand, int bound)
	{
		if (!ProjectEConfig.enableTimeWatch || this.angler == null || this.angler.worldObj == null
				|| this.angler.worldObj.loadedTileEntityList == null)
		{
			return rand.nextInt(bound);
		}
		Vec3 pos = this.angler.worldObj.getWorldVec3Pool().getVecFromPool(this.angler.posX, this.angler.posY, this.angler.posZ);
		for (Object obj : this.angler.worldObj.loadedTileEntityList)
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
					return rand.nextInt(Math.max(1, bound / 18));
				}
			}
		}
		return rand.nextInt(bound);
	}
}