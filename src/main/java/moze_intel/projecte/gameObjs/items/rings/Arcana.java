package moze_intel.projecte.gameObjs.items.rings;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import cpw.mods.fml.common.Optional;
import moze_intel.projecte.api.item.IExtraFunction;
import moze_intel.projecte.api.item.IModeChanger;
import moze_intel.projecte.api.item.IProjectileShooter;
import moze_intel.projecte.gameObjs.entity.EntityFireProjectile;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.items.IFireProtector;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.IconRegister;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.EnumChatFormatting;
import net.minecraft.Icon;
import net.minecraft.MathHelper;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class Arcana extends ItemPE implements IBauble, IModeChanger, IFlightProvider, IFireProtector, IExtraFunction, IProjectileShooter
{
	private Icon[] icons = new Icon[4];
	private Icon[] iconsOn = new Icon[4];
	
	public Arcana()
	{
		super();
		setUnlocalizedName("arcana_ring");
		setMaxStackSize(1);
		
		setContainerItem(this);
	}
	
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		return false;
	}

	@Override
	public byte getMode(ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		return stack.stackTagCompound.getByte("ArcanaMode");
	}

	@Override
	public void changeMode(EntityPlayer player, ItemStack stack)
	{
		if (stack.stackTagCompound == null)
		{
			stack.stackTagCompound = new net.minecraft.NBTTagCompound();
		}
		stack.stackTagCompound.setByte("ArcanaMode", (byte) ((getMode(stack) + 1) % 4));
	}
	
	private void tick(ItemStack stack, World world, ServerPlayer player)
	{
		if(stack.getTagCompound().getBoolean("Active"))
		{
			switch(getMode(stack))
			{
				case 0:
					WorldHelper.freezeInBoundingBox(world, player.boundingBox.expand(5, 5, 5), player, true);
					break;
				case 1:
					WorldHelper.igniteNearby(world, player);
					break;
				case 2:
					WorldHelper.growNearbyRandomly(true, world, player.posX, player.posY, player.posZ, player);
					break;
				case 3:
					WorldHelper.repelEntitiesInAABBFromPoint(world, player.boundingBox.expand(5, 5, 5), player.posX, player.posY, player.posZ, true);
					break;
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held)
	{
		if(stack.stackTagCompound == null) stack.setTagCompound(new NBTTagCompound());
		
		if(world.isRemote || slot > 8 || !(entity instanceof ServerPlayer)) return;
		
		tick(stack, world, (ServerPlayer)entity);
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public BaubleType getBaubleType(ItemStack stack)
	{
		return BaubleType.RING;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase entity)
	{
		if(stack.stackTagCompound == null) stack.setTagCompound(new NBTTagCompound());
		
		if(entity.worldObj.isRemote || !(entity instanceof ServerPlayer)) return;
		
		tick(stack, entity.worldObj, (ServerPlayer)entity);
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onEquipped(ItemStack stack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canEquip(ItemStack stack, EntityLivingBase player)
	{
		return true;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canUnequip(ItemStack stack, EntityLivingBase player)
	{
		return true;
	}

	@Override
	public Icon getIconFromSubtype(int damage)
	{
		return (iconsOn != null ? iconsOn : icons)[MathHelper.clamp_int(damage, 0, 3)];
	}

	@Override
	public void registerIcons(IconRegister register)
	{
		for(int i = 0; i < 4; i++)
		{
			icons[i] = register.registerIcon(this.getTexture("rings", "arcana_" + i));
		}
		
		for(int i = 0; i < 4; i++)
		{
			iconsOn[i] = register.registerIcon(this.getTexture("rings", "arcana_" + i + "_on"));
		}
		
		itemIcon = icons[0];
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b, net.minecraft.Slot slot)
	{
		if(stack.hasTagCompound())
		{
			if(!stack.stackTagCompound.getBoolean("Active"))
			{
				list.add(EnumChatFormatting.RED + StatCollector.translateToLocal("pe.arcana.inactive"));
			}
			else
			{
				list.add(StatCollector.translateToLocal("pe.arcana.mode") + EnumChatFormatting.AQUA + StatCollector.translateToLocal("pe.arcana.mode." + getMode(stack)));
			}
		}
	}

	@Override
	public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down){

		ItemStack stack = player.getHeldItemStack();
		World world = player.worldObj;
		if(!world.isRemote)
		{
			NBTTagCompound compound = stack.getTagCompound();
			
			compound.setBoolean("Active", !compound.getBoolean("Active"));
		}
		
		return true;
	}

	@Override
	public void doExtraFunction(ItemStack stack, EntityPlayer player) // GIANT FIRE ROW OF DEATH
	{
		World world = player.worldObj;
		
		if(world.isRemote) return;
		
		switch(getMode(stack))
		{
			case 1: // ignition
				switch(MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5) & 3)
				{
					case 0: // south, -z
					case 2: // north, +z
						for(int x = (int) (player.posX - 30); x <= player.posX + 30; x++)
							for(int y = (int) (player.posY - 5); y <= player.posY + 5; y++)
								for(int z = (int) (player.posZ - 3); z <= player.posZ + 3; z++)
									if(world.isAirBlock(x, y, z))
									{
										PlayerHelper.checkedPlaceBlock(((ServerPlayer) player), x, y, z, Blocks.fire, 0);
									}
						break;
					case 1: // west, -x
					case 3: // east, +x
						for(int x = (int) (player.posX - 3); x <= player.posX + 3; x++)
							for(int y = (int) (player.posY - 5); y <= player.posY + 5; y++)
								for(int z = (int) (player.posZ - 30); z <= player.posZ + 30; z++)
								{
									if(world.isAirBlock(x, y, z))
									{
										PlayerHelper.checkedPlaceBlock(((ServerPlayer) player), x, y, z, Blocks.fire, 0);
									}
								}
						break;
				}
				break;
		}
	}

	@Override
	public boolean shootProjectile(EntityPlayer player, ItemStack stack)
	{
		World world = player.worldObj;
		
		if(world.isRemote) return false;
		
		switch(getMode(stack))
		{
			case 0: // zero
				EntitySnowball snowball = new EntitySnowball(world, player);
				world.spawnEntityInWorld(snowball);
				world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F);
				break;
			case 1: // ignition
				EntityFireProjectile fire = new EntityFireProjectile(world, player);
				world.spawnEntityInWorld(fire);
				world.playSoundAtEntity(player, "projecte:item.pepower", 1.0F, 1.0F);
				break;
			case 3: // swrg
				EntitySWRGProjectile lightning = new EntitySWRGProjectile(world, player);
				world.spawnEntityInWorld(lightning);
				// world.playSoundAtEntity(player, "projecte:item.pewindmagic", 1.0F, 1.0F);
				break;
		}
		
		return true;
	}

	@Override
	public boolean canProtectAgainstFire(ItemStack stack, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean canProvideFlight(ItemStack stack, ServerPlayer player)
	{
		return true;
	}
}
