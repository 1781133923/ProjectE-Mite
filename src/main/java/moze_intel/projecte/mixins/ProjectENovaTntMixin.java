package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.blocks.NovaCatalyst;
import moze_intel.projecte.gameObjs.blocks.NovaCataclysm;
import moze_intel.projecte.gameObjs.entity.EntityNovaCatalystPrimed;
import moze_intel.projecte.gameObjs.entity.EntityNovaCataclysmPrimed;
import net.minecraft.Block;
import net.minecraft.BlockTNT;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityTNTPrimed;
import net.minecraft.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MITE's flint & steel and fire logic only ignite the vanilla Block.tnt
 * identity, so the ProjectE nova blocks can never be lit through those paths.
 * Intercept BlockTNT.ignite/primeTnt (used by flint & steel, fire, redstone
 * and burning arrows) and prime the corresponding nova entity instead.
 */
@Mixin(BlockTNT.class)
public abstract class ProjectENovaTntMixin
{
	@Inject(method = "ignite(Lnet/minecraft/World;IIILnet/minecraft/EntityLivingBase;)V",
			at = @At("HEAD"), cancellable = true)
	private static void projecte$igniteNova(World world, int x, int y, int z, EntityLivingBase entity, CallbackInfo ci)
	{
		if (projecte$primeNovaIfApplicable(world, x, y, z, entity))
		{
			ci.cancel();
		}
	}

	@Inject(method = "primeTnt(Lnet/minecraft/World;IIIILnet/minecraft/EntityLivingBase;)V",
			at = @At("HEAD"), cancellable = true)
	private static void projecte$primeNova(World world, int x, int y, int z, int metadata, EntityLivingBase entity,
			CallbackInfo ci)
	{
		if (projecte$primeNovaIfApplicable(world, x, y, z, entity))
		{
			ci.cancel();
		}
	}

	private static boolean projecte$primeNovaIfApplicable(World world, int x, int y, int z, EntityLivingBase entity)
	{
		if (world == null || world.isRemote)
		{
			return false;
		}
		Block block = world.getBlock(x, y, z);
		if (block instanceof NovaCatalyst)
		{
			if (entity == null)
			{
				entity = world.getClosestPlayer(x + 0.5D, y + 0.5D, z + 0.5D, 64.0D, false);
			}
			EntityTNTPrimed primed = block instanceof NovaCataclysm
					? new EntityNovaCataclysmPrimed(world, x + 0.5D, y + 0.5D, z + 0.5D, entity)
					: new EntityNovaCatalystPrimed(world, x + 0.5D, y + 0.5D, z + 0.5D, entity);
			world.spawnEntityInWorld(primed);
			world.playSoundAtEntity(primed, "game.tnt.primed", 1.0F, 1.0F);
			world.setBlockToAir(x, y, z);
			return true;
		}
		return false;
	}
}
