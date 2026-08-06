package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.blocks.NovaCatalyst;
import net.minecraft.Block;
import net.minecraft.ItemFlintAndSteel;
import net.minecraft.RaycastCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MITE's flint & steel only ignites the exact vanilla Block.tnt identity; the
 * ProjectE nova blocks (subclasses of BlockTNT) fail that check, so flint and
 * steel cannot light them (redstone still works because it calls
 * BlockTNT.ignite directly). Present the nova block as Block.tnt for that one
 * identity check; the BlockTNT.ignite call then routes through
 * ProjectENovaTntMixin, which primes the correct nova entity.
 */
@Mixin(ItemFlintAndSteel.class)
public abstract class ProjectEFlintAndSteelMixin
{
	@Redirect(method = "onItemRightClick(Lnet/minecraft/EntityPlayer;FZ)Z",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/RaycastCollision;getBlockHit()Lnet/minecraft/Block;"))
	private Block projecte$novaAsTnt(RaycastCollision rc)
	{
		Block block = rc.getBlockHit();
		return block instanceof NovaCatalyst ? net.minecraft.Block.tnt : block;
	}
}
