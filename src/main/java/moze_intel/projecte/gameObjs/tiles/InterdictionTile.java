package moze_intel.projecte.gameObjs.tiles;

import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.TileEntity;
import net.minecraft.AxisAlignedBB;

public class InterdictionTile extends TileEntity
{
	private AxisAlignedBB effectBounds = null;
	
	public void updateEntity()
	{
		if (effectBounds == null)
		{
			effectBounds = AxisAlignedBB.getBoundingBox(xCoord - 8, yCoord - 8, zCoord - 8, xCoord + 8, yCoord + 8, zCoord + 8);
		}
		WorldHelper.repelEntitiesInAABBFromPoint(worldObj, effectBounds, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, false);
	}
}
