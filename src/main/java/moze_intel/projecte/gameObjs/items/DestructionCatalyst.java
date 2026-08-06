package moze_intel.projecte.gameObjs.items;

import com.google.common.collect.Lists;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.ParticlePKT;
import moze_intel.projecte.utils.Coordinates;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.IconRegister;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.ItemStack;
import net.minecraft.AxisAlignedBB;
import net.minecraft.MovingObjectPosition;

import net.minecraft.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

public class DestructionCatalyst extends ItemCharge
{
	public DestructionCatalyst() 
	{
		super("destruction_catalyst", (byte)3);
		
	}

	// Only for Catalitic Lens
	protected DestructionCatalyst(String name, byte numCharges)
	{
		super(name, numCharges);
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (world.isRemote) return true;

		MovingObjectPosition mop = moze_intel.projecte.compat.PECompatHelper.getMovingObjectPositionFromPlayer(world, player);

		if (mop != null && mop.getEntityHit() == null)
		{
			int numRows = calculateDepthFromCharge(stack);
			boolean hasAction = false;
			
			ForgeDirection direction = ForgeDirection.getOrientation(mop.sideHit);
			
			Coordinates coords = new Coordinates(mop);
			AxisAlignedBB box = WorldHelper.getDeepBox(coords, direction, --numRows);
			
			List<ItemStack> drops = Lists.newArrayList();
			
			for (int x = (int) box.minX; x <= box.maxX; x++)
				for (int y = (int) box.minY; y <= box.maxY; y++)
					for (int z = (int) box.minZ; z <= box.maxZ; z++)
					{
						Block block = world.getBlock(x, y, z);
						if (block == null) // MITE represents air as null
						{
							continue;
						}
						float hardness = block.getBlockHardness(world.getBlockMetadata(x, y, z));
						
						if (hardness >= 50.0F || hardness == -1.0F)
						{
							continue;
						}
						
						if (!consumeFuel(player, stack, 8, true))
						{
							break;
						}
						
						if (!hasAction)
						{
							hasAction = true;
						}

						if (PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
						{
							List<ItemStack> list = WorldHelper.getBlockDrops(world, player, block, stack, x, y, z);
							if (list != null && list.size() > 0)
                            {
                                drops.addAll(list);
                            }

							world.setBlockToAir(x, y, z);

							if (world.rand.nextInt(8) == 0)
                            {
                                PacketHandler.sendToAllAround(new ParticlePKT("largesmoke", x, y, z), new TargetPoint(world.provider.dimensionId, x, y + 1, z, 32));
                            }
						}
					}

			PlayerHelper.swingItem(player);
			if (hasAction)
			{
				WorldHelper.createLootDrop(drops, world, mop.blockX, mop.blockY, mop.blockZ);
				world.playSoundAtEntity(player, "projecte:item.pedestruct", 1.0F, 1.0F);
			}
		}
			
		return true;
	}

	protected int calculateDepthFromCharge(ItemStack stack)
	{
		byte charge = getCharge(stack);
		if (charge <= 0)
		{
			return 1;
		}
		if (this instanceof CataliticLens)
		{
			return 8 + (charge * 8); // Increases linearly by 8, starting at 16 for charge 1

		}
		return (int) Math.pow(2, 1 + charge); // Default DesCatalyst formula, doubles for every level, starting at 4 for charge 1
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.itemIcon = register.registerIcon(this.getTexture("destruction_catalyst"));
	}
}
