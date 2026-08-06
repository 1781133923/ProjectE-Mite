package moze_intel.projecte.gameObjs.entity;

import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.Block;
import net.minecraft.EntityPlayer;
import net.minecraft.MathHelper;
import net.minecraft.ServerPlayer;
import net.minecraft.EntityThrowable;
import net.minecraft.ItemStack;
import net.minecraft.MovingObjectPosition;
import net.minecraft.RaycastCollision;
import net.minecraft.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class PEProjectile extends EntityThrowable
{
    public PEProjectile(World world)
    {
        super(world);
    }

    public PEProjectile(World world, EntityPlayer entity)
    {
        super(world, entity);
    }

    public PEProjectile(World world, double x, double y, double z)
    {
        super(world, x, y, z);
    }


    @Override
    protected void onImpact(RaycastCollision collision)
    {
        MovingObjectPosition mop;
        if (collision.isEntity()) {
            mop = new MovingObjectPosition(collision.getEntityHit(), 0.0D);
        } else {
            mop = new MovingObjectPosition(collision.world, collision.block_hit_x, collision.block_hit_y,
                    collision.block_hit_z, collision.face_hit == null ? 0 : collision.face_hit.ordinal(), collision.position_hit);
        }
        if (getThrower() instanceof ServerPlayer)
        {
            apply(mop);
        }
        this.setDead();
    }

    @Override
    public float getGravityVelocity()
    {
        return 0;
    }

    protected abstract void apply(MovingObjectPosition mop);

    /**
     * Places the given fluid at the projectile's impact position. MITE's
     * RaycastCollision face data is unreliable for throwables, so instead of
     * offsetting from the hit face (which could put the fluid inside a solid
     * block, silently failing for water or appearing "behind" the wall for
     * lava) this uses the block the projectile is in and falls back to the
     * nearest air neighbour.
     */
    protected final void placeFluidAtImpact(Block fluid)
    {
        World world = this.worldObj;
        int x = MathHelper.floor_double(this.posX);
        int y = MathHelper.floor_double(this.posY);
        int z = MathHelper.floor_double(this.posZ);
        ServerPlayer player = (ServerPlayer) getThrower();

        if (world.isAirBlock(x, y, z))
        {
            PlayerHelper.checkedPlaceBlock(player, x, y, z, fluid, 0);
            return;
        }
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            int nx = x + dir.offsetX;
            int ny = y + dir.offsetY;
            int nz = z + dir.offsetZ;
            if (world.isAirBlock(nx, ny, nz))
            {
                PlayerHelper.checkedPlaceBlock(player, nx, ny, nz, fluid, 0);
                return;
            }
        }
    }

    protected final boolean tryConsumeEmc(ItemPE consumeFrom, double amount)
    {
        EntityPlayer player = ((EntityPlayer) getThrower());
        ItemStack found = PlayerHelper.findFirstItem(player, consumeFrom);
        return found != null && ItemPE.consumeFuel(player, found, amount, true);
    }
}
