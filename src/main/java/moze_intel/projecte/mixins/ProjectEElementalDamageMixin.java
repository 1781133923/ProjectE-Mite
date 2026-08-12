package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.DamageSource;
import net.minecraft.EntityEarthElemental;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MITE's earth elemental is only damageable by ItemTool subclasses whose
 * material is effective against the elemental's block (hammer/pick for stone,
 * obsidian, netherrack and end stone; axe for planks). ProjectE tools extend
 * ItemMode instead of ItemTool, so they are treated as immune. Re-route the
 * check through PEToolBase's own material logic.
 */
@Mixin(EntityEarthElemental.class)
public abstract class ProjectEElementalDamageMixin {
    @Inject(method = "isImmuneTo", at = @At("HEAD"), cancellable = true)
    private void projecte$allowPETools(DamageSource damage_source, CallbackInfoReturnable<Boolean> cir) {
        EntityEarthElemental self = (EntityEarthElemental) (Object) this;
        ItemStack stack = damage_source.getItemAttackedWith();
        if (stack == null || !(stack.getItem() instanceof PEToolBase)) {
            return;
        }
        PEToolBase tool = (PEToolBase) stack.getItem();
        // The red matter katar is crafted with a red matter hammer (and an
        // axe), so its melee and C-key special attack damage every earth
        // elemental variant regardless of the elemental's block.
        if (tool instanceof moze_intel.projecte.gameObjs.items.tools.RedKatar)
        {
            cir.setReturnValue(false);
            return;
        }
        net.minecraft.Block block = self.getBlock();
        if (block != null && tool.isEffectiveAgainstBlock(block, 0)) {
            cir.setReturnValue(false);
        }
    }

}
