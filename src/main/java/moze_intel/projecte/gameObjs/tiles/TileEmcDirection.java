package moze_intel.projecte.gameObjs.tiles;

import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.OrientationSyncPKT;
import net.minecraft.EntityLivingBase;
import net.minecraft.NBTTagCompound;

import net.minecraft.Packet;
import net.minecraft.Packet132TileEntityData;
import net.minecraft.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileEmcDirection extends TileEmc
{
	private ForgeDirection orientation;
	
	public TileEmcDirection()
	{
		this.orientation = ForgeDirection.SOUTH;
	}
	
	public ForgeDirection getOrientation()
	{
		return orientation;
	}

	public void setOrientation(ForgeDirection orientation)
	{
		this.orientation = orientation;
	}

	public void setOrientation(int orientation)
	{
		this.orientation = ForgeDirection.getOrientation(orientation);
	}
	
	public void setRelativeOrientation(EntityLivingBase ent, boolean sendPacket)
	{
		int direction = 0;
		int facing = MathHelper.floor_double(ent.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if (facing == 0)
		{
			direction = ForgeDirection.NORTH.ordinalValue;
		}
		else if (facing == 1)
		{
			direction = ForgeDirection.EAST.ordinalValue;
		}
		else if (facing == 2)
		{
			direction = ForgeDirection.SOUTH.ordinalValue;
		}
		else if (facing == 3)
		{
			direction = ForgeDirection.WEST.ordinalValue;
		}
		
		setOrientation(direction);
		
		if (sendPacket)
		{
			PacketHandler.sendToAll(new OrientationSyncPKT(this, direction));
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound)
	{
		super.readFromNBT(nbtTagCompound);

		if (nbtTagCompound.hasKey("Direction"))
		{
			this.orientation = ForgeDirection.getOrientation(nbtTagCompound.getByte("Direction"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound)
	{
		super.writeToNBT(nbtTagCompound);

		nbtTagCompound.setByte("Direction", (byte) orientation.ordinalValue);
	}
	
	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tag);
	}
		
}
