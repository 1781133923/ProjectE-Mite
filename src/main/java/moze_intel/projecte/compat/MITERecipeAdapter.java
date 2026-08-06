package moze_intel.projecte.compat;

import net.minecraft.CraftingResult;
import net.minecraft.IRecipe;
import net.minecraft.InventoryCrafting;
import net.minecraft.ItemStack;
import net.minecraft.Material;

/**
 * Bridges 1.7.10-style custom recipes into MITE's extended IRecipe contract.
 */
public abstract class MITERecipeAdapter implements IRecipe {
    private float difficulty = 1.0F;
    private int[] skillsets;
    private Material material;

    @Override
    public CraftingResult getCraftingResult(InventoryCrafting inv) {
        ItemStack output = this.getCraftingOutput(inv);
        if (output == null) {
            output = this.getRecipeOutput();
        }
        return new CraftingResult(output == null ? null : output.copy(), this.difficulty, this.skillsets, this);
    }

    /**
     * 1.7.10-style crafting result.
     */
    public abstract ItemStack getCraftingOutput(InventoryCrafting inv);

    @Override
    public ItemStack[] getComponents() {
        return new ItemStack[0];
    }

    @Override
    public IRecipe setDifficulty(float difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    @Override
    public IRecipe scaleDifficulty(float factor) {
        this.difficulty *= factor;
        return this;
    }

    @Override
    public float getUnmodifiedDifficulty() {
        return this.difficulty;
    }

    @Override
    public void setIncludeInLowestCraftingDifficultyDetermination() {
    }

    @Override
    public boolean getIncludeInLowestCraftingDifficultyDetermination() {
        return false;
    }

    @Override
    public void setSkillsets(int[] skillsets) {
        this.skillsets = skillsets;
    }

    @Override
    public void setSkillset(int skillset) {
        this.skillsets = new int[]{skillset};
    }

    @Override
    public int[] getSkillsets() {
        return this.skillsets;
    }

    @Override
    public void setMaterialToCheckToolBenchHardnessAgainst(Material material) {
        this.material = material;
    }

    @Override
    public Material getMaterialToCheckToolBenchHardnessAgainst() {
        return this.material;
    }
}
