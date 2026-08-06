package moze_intel.projecte.gameObjs.items.rings;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.item.IModeChanger;
import moze_intel.projecte.api.item.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.entity.EntityHomingArrow;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.utils.EMCHelper;
import moze_intel.projecte.utils.MathUtils;
import net.minecraft.IconRegister;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityLiving;
import net.minecraft.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.ItemStack;
import net.minecraft.EnumChatFormatting;
import net.minecraft.Icon;
import net.minecraft.StatCollector;
import net.minecraft.World;
import net.minecraft.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class ArchangelSmite extends RingToggle implements IPedestalItem, IModeChanger, IBauble
{
	public ArchangelSmite()
	{
		super("archangel_smite");
		this.setMaxStackSize(1);
		
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5)
	{
		if (!world.isRemote && getMode(stack) == 1 && entity instanceof EntityLivingBase && par4 <= 8)
		{
			fireArrow(stack, world, ((EntityLivingBase) entity));
		}
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if (!world.isRemote)
		{
			fireArrow(stack, world, player);
		}
		return true;
	}

	private void fireArrow(ItemStack ring, World world, EntityLivingBase shooter)
	{
		EntityHomingArrow arrow = new EntityHomingArrow(world, shooter, 2.0F);

		if (!(shooter instanceof EntityPlayer) || consumeFuel(((EntityPlayer) shooter), ring, EMCHelper.getEmcValue(Items.arrow), true))
		{
			world.playSoundAtEntity(shooter, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + 0.5F);
			world.spawnEntityInWorld(arrow);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		this.itemIcon = register.registerIcon(this.getTexture("rings", "archangel_smite"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromSubtype(int dmg)
	{
		return itemIcon;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.RING;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase player)
	{
		this.onUpdate(stack, player.worldObj, player, 0, false);
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player)
	{
		return true;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canUnequip(ItemStack itemstack, EntityLivingBase player)
	{
		return true;
	}

	@Override
	public void updateInPedestal(World world, int x, int y, int z)
	{
		if (!world.isRemote && ProjectEConfig.archangelPedCooldown != -1)
		{
			DMPedestalTile tile = ((DMPedestalTile) world.getBlockTileEntity(x, y, z));
			if (tile.getActivityCooldown() == 0)
			{
				if (!world.getEntitiesWithinAABB(EntityLiving.class, tile.getEffectBounds()).isEmpty())
				{
					for (int i = 0; i < 3; i++)
					{
						EntityHomingArrow arrow = new EntityHomingArrow(world, FakePlayerFactory.get(((WorldServer) world), PECore.FAKEPLAYER_GAMEPROFILE), 2.0F);
						arrow.posX = tile.centeredX;
						arrow.posY = tile.centeredY + 2;
						arrow.posZ = tile.centeredZ;
						arrow.motionX = 0;
						arrow.motionZ = 0;
						arrow.motionY = 1;
						world.playSoundAtEntity(arrow, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + 0.5F);
						world.spawnEntityInWorld(arrow);
					}
				}
				tile.setActivityCooldown(ProjectEConfig.archangelPedCooldown);
			}
			else
			{
				tile.decrementActivityCooldown();
			}
		}
	}

	@Override
	public List<String> getPedestalDescription()
	{
		List<String> list = Lists.newArrayList();
		if (ProjectEConfig.archangelPedCooldown != -1) {
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.archangel.pedestal1"));
			list.add(EnumChatFormatting.BLUE + String.format(
					StatCollector.translateToLocal("pe.archangel.pedestal2"), MathUtils.tickToSecFormatted(ProjectEConfig.archangelPedCooldown)));
		}
		return list;
	}

	@Override
	@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean par4, net.minecraft.Slot slot)
	{
		super.addInformation(stack, player, list, par4, slot);
		list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.archangel.tooltip1"));
	}
}
