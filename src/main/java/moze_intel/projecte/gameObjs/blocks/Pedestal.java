package moze_intel.projecte.gameObjs.blocks;


import cpw.mods.fml.common.network.NetworkRegistry;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.gameObjs.tiles.TileEmc;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.SyncPedestalPKT;
import moze_intel.projecte.utils.Constants;
import moze_intel.projecte.utils.PELogger;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.BlockContainer;
import net.minecraft.BlockConstants;
import net.minecraft.Material;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.TileEntity;
import net.minecraft.IBlockAccess;
import net.minecraft.World;

public class Pedestal extends BlockContainer {

    public Pedestal() {
        super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextBlockID(), Material.stone, new BlockConstants());
        this.setCreativeTab(ObjHandler.cTab);
        this.setHardness(1.0F);
        this.setBlockBoundsForAllThreads(0.1875F, 0.0F, 0.1875F, 0.8125F, 0.75F, 0.8125F);
        ((net.xiaoyu233.fml.api.block.IBlock) this).setBlockTextureName(PECore.MODID.toLowerCase() + ":dm");
        setUnlocalizedName("pe_dmPedestal");
    }

    @Override
    public int getRenderType()
    {
        return 22;
    }

    @Override
    public boolean shouldSideBeRendered(net.minecraft.IBlockAccess world, int x, int y, int z, int side)
    {
        // Always render every face so the non-full-cube pedestal does not show
        // a see-through gap at the ground contact face.
        return true;
    }

    @Override
    public boolean isStandardFormCube(boolean[] isSolid, int metadata)
    {
        // Not a full cube: prevent MITE from culling neighbouring blocks'
        // faces that face this pedestal.
        return false;
    }

    public void breakBlock(World world, int x, int y, int z, int blockID, int meta)
    {
        DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
        // CarryOn (and other pickup mods) remove the tile entity before
        // setBlockToAir, so the tile can legitimately be null here - guard it
        // like the other ProjectE container blocks do.
        if (tile != null)
        {
            if (tile.getItemStack() != null)
            {
                WorldHelper.spawnEntityItem(world, tile.getItemStack().copy(), x, y, z);
            }
            tile.invalidate();
        }
        super.breakBlock(world, x, y, z, blockID, meta);
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, net.minecraft.EnumFace face, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
            if (player.isSneaking())
            {
                moze_intel.projecte.compat.PEGuiHelper.openGui(player, Constants.PEDESTAL_GUI, world, x, y, z);
            }
            else
            {
                if (tile.getItemStack() != null && tile.getItemStack().getItem() instanceof IPedestalItem)
                {
                    tile.setActive(!tile.getActive());
                }
                PELogger.logDebug("Pedestal: " + (tile.getActive() ? "ON" : "OFF"));
            }
            PacketHandler.sendToAllAround(new SyncPedestalPKT(tile), new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, 32));
        }
        return true;
    }

	@Override
	public boolean onBlockPlacedMITE(World world, int x, int y, int z, int metadata, net.minecraft.Entity placer, boolean test_only)
	{
		return true;
	}

    @Override
    public boolean hasTileEntity()
    {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return new DMPedestalTile();
    }
}
