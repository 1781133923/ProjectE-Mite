package moze_intel.projecte.rendering;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.tiles.AlchChestTile;
import net.minecraft.ModelChest;
import net.minecraft.TileEntitySpecialRenderer;
import net.minecraft.TileEntity;
import net.minecraft.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class ChestRenderer extends TileEntitySpecialRenderer
{
	private final ResourceLocation texture = new ResourceLocation(PECore.MODID.toLowerCase(), "textures/blocks/alchemy_chest.png");
	private final ModelChest model = new ModelChest();
	
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float var8) 
	{
		if (!(tile instanceof AlchChestTile)) 
		{
			return;
		}
		
		AlchChestTile chestTile = (AlchChestTile) tile;
		ForgeDirection direction = null;
		
		if (chestTile.getWorldObj() != null)
		{
			direction = chestTile.getOrientation();
			// Block metadata (2-5) is the reliable facing source (vanilla
			// chest / matter furnace convention); the tile orientation is the
			// fallback for blocks placed before this change.
			int facing = chestTile.getWorldObj().getBlockMetadata(chestTile.xCoord, chestTile.yCoord, chestTile.zCoord);
			// setFacingMeta convention: 2=NORTH, 3=SOUTH, 4=WEST, 5=EAST.
			switch (facing)
			{
				case 2: direction = net.minecraftforge.common.util.ForgeDirection.NORTH; break;
				case 3: direction = net.minecraftforge.common.util.ForgeDirection.SOUTH; break;
				case 4: direction = net.minecraftforge.common.util.ForgeDirection.WEST; break;
				case 5: direction = net.minecraftforge.common.util.ForgeDirection.EAST; break;
			}
		}
		this.bindTexture(texture);
		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glTranslatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
		GL11.glScalef(1.0F, -1.0F, -1.0F);
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		short angle = 0;

		if (direction != null)
		{
			if (direction == ForgeDirection.NORTH)
			{
				angle = 180;
			}
			else if (direction == ForgeDirection.SOUTH)
			{
				angle = 0;
			}
			else if (direction == ForgeDirection.WEST)
			{
				angle = 90;
			}
			else if (direction == ForgeDirection.EAST)
			{
				angle = -90;
			}
		}

		GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		float adjustedLidAngle = chestTile.prevLidAngle + (chestTile.lidAngle - chestTile.prevLidAngle) * var8;
		adjustedLidAngle = 1.0F - adjustedLidAngle;
		adjustedLidAngle = 1.0F - adjustedLidAngle * adjustedLidAngle * adjustedLidAngle;
		model.chestLid.rotateAngleX = -(adjustedLidAngle * (float) Math.PI / 2.0F);
		model.renderAll();
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
