package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.Block;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.MovingObjectPosition;

import net.minecraft.World;
import net.minecraft.WorldInfo;
import net.minecraftforge.common.util.ForgeDirection;

public class EntityWaterProjectile extends PEProjectile
{
	public EntityWaterProjectile(World world)
	{
		super(world);
	}

	public EntityWaterProjectile(World world, EntityPlayer entity)
	{
		super(world, entity);
	}

	public EntityWaterProjectile(World world, double x, double y, double z)
	{
		super(world, x, y, z);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (!this.worldObj.isRemote)
		{
			if (ticksExisted > 400 || !this.worldObj.blockExists(((int) this.posX), ((int) this.posY), ((int) this.posZ)))
			{
				this.setDead();
				return;
			}

			if (getThrower() instanceof ServerPlayer)
			{
				ServerPlayer player = ((ServerPlayer) getThrower());
				for (int x = (int) (this.posX - 3); x <= this.posX + 3; x++)
					for (int y = (int) (this.posY - 3); y <= this.posY + 3; y++)
						for (int z = (int) (this.posZ - 3); z <= this.posZ + 3; z++)
						{
							Block block = this.worldObj.getBlock(x, y, z);

							if (block == Blocks.lava)
							{
								PlayerHelper.checkedReplaceBlock(player, x, y, z, Blocks.obsidian, 0);
								this.worldObj.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.fizz", 0.5F, 2.6F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.8F);
							}
							else if (block == Blocks.flowing_lava)
							{
								PlayerHelper.checkedReplaceBlock(player, x, y, z, Blocks.cobblestone, 0);
								this.worldObj.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "random.fizz", 0.5F, 2.6F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.8F);
							}

						}
			}

			if (this.isInWater())
			{
				this.setDead();
			}
			
			if (this.posY > 128)
			{
				WorldInfo worldInfo = this.worldObj.getWorldInfo();
				moze_intel.projecte.compat.PECompatHelper.setRaining(worldObj, true);
				this.setDead();
			}
		}
	}

	@Override
	protected void apply(MovingObjectPosition mop)
	{
		if (this.worldObj.isRemote)
		{
			return;
		}

		if (mop.getEntityHit() == null)
		{
			placeFluidAtImpact(Blocks.flowing_water);
		}
		else if (mop.getEntityHit() != null)
		{
			Entity ent = mop.getEntityHit();

			if (ent.isBurning())
			{
				ent.extinguish();
			}

			ent.addVelocity(this.motionX * 2, this.motionY * 2, this.motionZ * 2);
		}
	}
}
