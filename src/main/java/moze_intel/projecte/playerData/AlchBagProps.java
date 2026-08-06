package moze_intel.projecte.playerData;

import com.google.common.collect.Maps;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.PELogger;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.NBTTagList;
import net.minecraft.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.Map;

public class AlchBagProps implements IExtendedEntityProperties
{
	public static final String PROP_NAME = "ProjectEAlchBag";

	private final EntityPlayer player;
	private final Map<Integer, ItemStack[]> bagData = Maps.newHashMap();

	public static void register(EntityPlayer player)
	{
		moze_intel.projecte.compat.ExtendedProperties.register(player, PROP_NAME, new AlchBagProps(player));
	}

	public static AlchBagProps getDataFor(EntityPlayer player)
	{
		net.minecraftforge.common.IExtendedEntityProperties props =
				moze_intel.projecte.compat.ExtendedProperties.get(player, PROP_NAME);
		if (props == null)
		{
			register(player);
			props = moze_intel.projecte.compat.ExtendedProperties.get(player, PROP_NAME);
		}
		return ((AlchBagProps) props);
	}

	public AlchBagProps(EntityPlayer player)
	{
		this.player = player;
	}

	protected ItemStack[] getInv(int color)
	{
		if (bagData.get(color) == null)
		{
			bagData.put(color, new ItemStack[104]);
			PELogger.logInfo("Created new inventory array for color " + color + " and player " + player.getCommandSenderName());
		}
		ItemStack[] inv = bagData.get(color);
		return Arrays.copyOf(inv, inv.length);
	}

	protected void setInv(int color, ItemStack[] inv)
	{
		bagData.put(color, inv);
	}

	protected NBTTagCompound saveForPacket()
	{
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList listOfInventories = new NBTTagList();
		for (int i = 0; i < 16; i++)
		{
			if (bagData.get(i) == null)
			{
				continue;
			}
			NBTTagCompound inventory = new NBTTagCompound();
			inventory.setInteger("color", i);
			inventory.setTag("inv", ItemHelper.toIndexedNBTList(bagData.get(i)));
			listOfInventories.appendTag(inventory);
		}
		compound.setTag("data", listOfInventories);
		return compound;
	}
	/**
	 * Only write one bag's data. Used for partial sync packets
	 */
	protected NBTTagCompound saveForPartialPacket(int color)
	{
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList listOfInventories = new NBTTagList();
		if (bagData.get(color) == null)
		{
			return compound;
		}
		NBTTagCompound inventory = new NBTTagCompound();
		inventory.setInteger("color", color);
		inventory.setTag("inv", ItemHelper.toIndexedNBTList(bagData.get(color)));
		listOfInventories.appendTag(inventory);
		compound.setTag("data", listOfInventories);
		return compound;
	}

	public void readFromPacket(NBTTagCompound compound)
	{
		NBTTagList listOfInventoies = compound.getTagList("data");
		for (int i = 0; i < listOfInventoies.tagCount(); i++)
		{
			NBTTagCompound inventory = ((NBTTagCompound) listOfInventoies.tagAt(i));
			bagData.put(inventory.getInteger("color"), ItemHelper.copyIndexedNBTToArray(inventory.getTagList("inv"), new ItemStack[104]));
		}
	}

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound properties = new NBTTagCompound();

		NBTTagList listOfInventories = new NBTTagList();
		for (int i = 0; i < 16; i++)
		{
			if (bagData.get(i) == null)
			{
				continue;
			}
			NBTTagCompound inventory = new NBTTagCompound();
			inventory.setInteger("color", i);
			inventory.setTag("inv", ItemHelper.toIndexedNBTList(bagData.get(i)));
			listOfInventories.appendTag(inventory);
		}

		properties.setTag("data", listOfInventories);
		compound.setTag(PROP_NAME, properties);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		NBTTagCompound properties = compound.getCompoundTag(PROP_NAME);

		// Same defensive clear as TransmutationProps: the in-memory bag data
		// may belong to a previous world and must not leak into this one.
		bagData.clear();
		NBTTagList listOfInventoies = properties.getTagList("data");
		for (int i = 0; i < listOfInventoies.tagCount(); i++)
		{
			NBTTagCompound inventory = ((NBTTagCompound) listOfInventoies.tagAt(i));
			bagData.put(inventory.getInteger("color"), ItemHelper.copyIndexedNBTToArray(inventory.getTagList("inv"), new ItemStack[104]));
		}
	}

	@Override
	public void init(Entity entity, World world) {}
}
