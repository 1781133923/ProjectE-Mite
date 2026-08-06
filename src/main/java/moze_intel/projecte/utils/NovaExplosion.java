package moze_intel.projecte.utils;

import net.minecraft.Block;
import net.minecraft.EnumParticle;
import net.minecraft.Entity;
import net.minecraft.ItemStack;
import net.minecraft.EntityLivingBase;
import net.minecraft.Explosion;
import net.minecraft.World;

import java.util.ArrayList;
import java.util.List;

/**
 * MITE port: uses the vanilla explosion machinery instead of reimplementing the
 * 1.7.10 ray-cast loop.
 */
public class NovaExplosion extends Explosion {
	private final World worldObj;
	private Explosion delegate;

	NovaExplosion(World world, Entity entity, double x, double y, double z, float radius)
	{
		super(world, entity, x, y, z, radius, radius);
		this.worldObj = world;
	}

	@Override
	public void doExplosionA() {
		this.delegate = this.worldObj.createExplosion(this.exploder, this.explosionX, this.explosionY, this.explosionZ,
				this.explosion_size_vs_blocks, this.explosion_size_vs_living_entities, this.isSmoking);
		if (this.delegate != null) {
			this.affectedBlockPositions = this.delegate.affectedBlockPositions;
		}
	}

	@Override
	public void doExplosionB(boolean spawnParticles) {
		if (this.delegate != null) {
			this.delegate.doExplosionB(spawnParticles);
		}
	}

	public EntityLivingBase getExplosivePlacedBy() {
		return this.exploder instanceof EntityLivingBase ? (EntityLivingBase) this.exploder : null;
	}

	public float getExplosionSize() {
		return this.explosion_size_vs_blocks;
	}
}
