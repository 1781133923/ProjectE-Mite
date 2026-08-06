package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.utils.PELogger;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.EnumParticle;
import net.minecraft.Material;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Entity;
import net.minecraft.EntityLiving;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityEnderman;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.EntityArrow;

import net.minecraft.AxisAlignedBB;
import net.minecraft.DamageSource;
import net.minecraft.MathHelper;
import net.minecraft.MovingObjectPosition;
import net.minecraft.Vec3;
import net.minecraft.World;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EntityHomingArrow extends EntityArrow
{
	private static final int DW_TARGET_ID = 31;
	private static final int NO_TARGET = -1;

	private int newTargetCooldown = 0;

	public EntityHomingArrow(World world)
	{
		super(world);
	}

	public EntityHomingArrow(World world, EntityLivingBase par2, float par3) 
	{
		super(world, par2, par3, net.minecraft.Item.arrowFlint, false);
	}

	@Override
	public void entityInit()
	{
		super.entityInit();
		dataWatcher.addObject(DW_TARGET_ID, NO_TARGET); // Target entity id
	}

	@Override
	public void onUpdate()
	{
		onEntityUpdate();

		this.canBePickedUp = 0;

		boolean inGround = WorldHelper.isArrowInGround(this);
		if (!worldObj.isRemote && this.ticksExisted > 3)
		{
			if (hasTarget() && (!getTarget().isEntityAlive() || inGround))
			{
				dataWatcher.updateObject(DW_TARGET_ID, NO_TARGET);
			}

			if (!hasTarget() && !inGround && newTargetCooldown <= 0)
			{
				findNewTarget();
			} else
			{
				newTargetCooldown--;
			}
		}

		if (ticksExisted > 3 && hasTarget() && !WorldHelper.isArrowInGround(this))
		{
			this.worldObj.spawnParticle(EnumParticle.flame, this.posX + this.motionX / 4.0D, this.posY + this.motionY / 4.0D, this.posZ + this.motionZ / 4.0D, -this.motionX / 2, -this.motionY / 2 + 0.2D, -this.motionZ / 2);
			this.worldObj.spawnParticle(EnumParticle.flame, this.posX + this.motionX / 4.0D, this.posY + this.motionY / 4.0D, this.posZ + this.motionZ / 4.0D, -this.motionX / 2, -this.motionY / 2 + 0.2D, -this.motionZ / 2);
			Entity target = getTarget();


			double dxToTarget = target.posX - posX;
			double dyToTarget = (target.boundingBox.minY + target.height) - posY;
			double dzToTarget = target.posZ - posZ;
			double mx = this.motionX;
			double my = this.motionY;
			double mz = this.motionZ;

			// Angle between the current motion and the direct line to the target
			double lenMotion = Math.sqrt(mx * mx + my * my + mz * mz);
			double lenLook = Math.sqrt(dxToTarget * dxToTarget + dyToTarget * dyToTarget + dzToTarget * dzToTarget);
			double theta = 0.0D;
			if (lenMotion > 1.0E-6D && lenLook > 1.0E-6D) {
				double dot = (mx * dxToTarget + my * dyToTarget + mz * dzToTarget) / (lenMotion * lenLook);
				theta = Math.acos(Math.max(-1.0D, Math.min(1.0D, dot)));
				if (Double.isNaN(theta)) {
					theta = 0.0D;
				}
			}
			theta = clampAbs(theta, Math.PI / 2);

			// Rotation axis = motion x look (cross product), normalized
			double ax = my * dzToTarget - mz * dyToTarget;
			double ay = mz * dxToTarget - mx * dzToTarget;
			double az = mx * dyToTarget - my * dxToTarget;
			double lenAxis = Math.sqrt(ax * ax + ay * ay + az * az);
			double adjustedX = mx;
			double adjustedY = my;
			double adjustedZ = mz;
			if (lenAxis > 1.0E-6D) {
				ax /= lenAxis;
				ay /= lenAxis;
				az /= lenAxis;
				double cosT = Math.cos(theta);
				double sinT = Math.sin(theta);
				double dotAxis = mx * ax + my * ay + mz * az;
				// Rodrigues' rotation formula
				adjustedX = mx * cosT + (ay * mz - az * my) * sinT + ax * dotAxis * (1.0D - cosT);
				adjustedY = my * cosT + (az * mx - ax * mz) * sinT + ay * dotAxis * (1.0D - cosT);
				adjustedZ = mz * cosT + (ax * my - ay * mx) * sinT + az * dotAxis * (1.0D - cosT);
			}

			setThrowableHeading(adjustedX, adjustedY, adjustedZ, 1.0F, 0);
			super.onUpdate();

//			old homing code (sucks)
//			double d5 = target.posX - this.posX;
//			double d6 = target.boundingBox.minY + target.height - this.posY;
//			double d7 = target.posZ - this.posZ;
//
//			this.setThrowableHeading(d5, d6, d7, 0.1F, 0.0F);
//			super.onUpdate();
		} else
		{
			super.onUpdate();
		}
	}

	private void findNewTarget()
	{
		List<EntityLiving> candidates = worldObj.getEntitiesWithinAABB(EntityLiving.class, this.boundingBox.expand(8, 8, 8));
		Collections.sort(candidates, new Comparator<EntityLiving>() {
			@Override
			public int compare(EntityLiving o1, EntityLiving o2) {
				double dist = EntityHomingArrow.this.getDistanceSqToEntity(o1) - EntityHomingArrow.this.getDistanceSqToEntity(o2);
				if (dist == 0.0)
				{
					return 0;
				} else
				{
					return dist > 0.0 ? 1 : -1;
				}
			}
		});

		if (!candidates.isEmpty())
		{
			dataWatcher.updateObject(DW_TARGET_ID, candidates.get(0).entityId);
		}

		newTargetCooldown = 5;
	}

	private EntityLiving getTarget()
	{
		return ((EntityLiving) worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(DW_TARGET_ID)));
	}

	private boolean hasTarget()
	{
		return getTarget() != null;
	}

	private double wrap180Radian(double radian)
	{
		radian %= 2 * Math.PI;

		while (radian >= Math.PI)
		{
			radian -= 2 * Math.PI;
		}

		while (radian < -Math.PI)
		{
			radian += 2 * Math.PI;
		}

		return radian;
	}

	private double clampAbs(double param, double maxMagnitude)
	{
		if (Math.abs(param) > maxMagnitude)
		{
			//System.out.println("CLAMPED");
			if (param < 0)
			{
				param = -Math.abs(maxMagnitude);
			} else
			{
				param = Math.abs(maxMagnitude);
			}
		}

		return param;
	}
}
