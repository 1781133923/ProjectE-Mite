package moze_intel.projecte.rendering;

import net.minecraft.Entity;
import net.minecraft.Render;
import net.minecraft.ResourceLocation;
import net.minecraft.Tessellator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Arrow-style renderer for the ProjectE projectile entities (water/lava orbs,
 * lens explosives, loot balls, fire/wind projectiles, ...). RenderSnowball
 * depends on the item-icon atlas, which MITE does not draw correctly for these
 * custom items; like RenderArrow this binds a dedicated texture directly and
 * draws a billboarded quad that always faces the player.
 */
public class PEProjectileRenderer extends Render
{
	private final ResourceLocation texture;

	public PEProjectileRenderer(String texturePath)
	{
		this.texture = new ResourceLocation("projecte", "textures/" + texturePath);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		this.bindEntityTexture(entity);
		drawBillboard(Tessellator.instance);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}

	private void drawBillboard(Tessellator tessellator)
	{
		GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		float half = 0.25F;
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		tessellator.addVertexWithUV(-half, -half, 0.0D, 0.0D, 1.0D);
		tessellator.addVertexWithUV(half, -half, 0.0D, 1.0D, 1.0D);
		tessellator.addVertexWithUV(half, half, 0.0D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(-half, half, 0.0D, 0.0D, 0.0D);
		tessellator.draw();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return this.texture;
	}
}
