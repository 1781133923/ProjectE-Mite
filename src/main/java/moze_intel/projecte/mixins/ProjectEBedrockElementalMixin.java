package moze_intel.projecte.mixins;

import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.DamageSource;
import net.minecraft.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BeyondExtreme's bedrock elemental overrides isImmuneTo() with its own
 * allow-list (vibranium pickaxe/war hammer etc.). ProjectE hammers/picks are
 * at least as strong, so let them through too. The mixin is @Pseudo: if
 * BeyondExtreme is not installed the target class does not exist and this
 * injection is skipped instead of failing the mod load.
 */
@Pseudo
@Mixin(targets = "net.moddedmite.mitemod.bex.entity.EntityBedrockElemental")
public abstract class ProjectEBedrockElementalMixin {
    @Inject(method = "isImmuneTo", at = @At("HEAD"), cancellable = true)
    private void projecte$allowPEHammerPick(DamageSource damage_source, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = damage_source.getItemAttackedWith();
        if (stack == null || !(stack.getItem() instanceof PEToolBase)) {
            return;
        }
        PEToolBase tool = (PEToolBase) stack.getItem();
        if (tool.isMiningWeapon()) {
            cir.setReturnValue(false);
        }
    }
}
