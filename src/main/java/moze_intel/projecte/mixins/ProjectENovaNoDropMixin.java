package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import moze_intel.projecte.gameObjs.entity.EntityNovaCataclysmPrimed;
import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nova Catalyst / Nova Cataclysm explosions should flatten the terrain without
 * leaving any dropped blocks or items behind. MITE spawns explosion drops
 * through Block.dropBlockAsEntityItem; many blocks override the 1-arg variant,
 * but every override funnels into the final (BlockBreakInfo, int, int, int,
 * float) method, so that is the reliable place to suppress drops. Keep the
 * 1-arg guard as well for blocks whose base implementation drops directly.
 */
@Mixin(Block.class)
public abstract class ProjectENovaNoDropMixin
{
	@Inject(method = "dropBlockAsEntityItem(Lnet/minecraft/BlockBreakInfo;)I",
			at = @At("HEAD"), cancellable = true)
	private void projecte$suppressNovaDrops(BlockBreakInfo info, CallbackInfoReturnable<Integer> cir)
	{
		if (isNovaExplosion(info))
		{
			cir.setReturnValue(0);
		}
	}

	@Inject(method = "dropBlockAsEntityItem(Lnet/minecraft/BlockBreakInfo;IIIF)I",
			at = @At("HEAD"), cancellable = true)
	private void projecte$suppressNovaDropsDeep(BlockBreakInfo info, int itemId, int damage, int count, float chance,
			CallbackInfoReturnable<Integer> cir)
	{
		if (isNovaExplosion(info))
		{
			cir.setReturnValue(0);
		}
	}

	private static boolean isNovaExplosion(BlockBreakInfo info)
	{
		if (info == null || info.explosion == null || info.explosion.exploder == null)
		{
			return false;
		}
		Object exploder = info.explosion.exploder;
		return exploder instanceof EntityNovaCatalystPrimed || exploder instanceof EntityNovaCataclysmPrimed;
	}
}
