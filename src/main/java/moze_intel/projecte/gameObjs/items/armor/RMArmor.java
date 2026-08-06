package moze_intel.projecte.gameObjs.items.armor;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.items.tools.PEToolMaterials;
import moze_intel.projecte.utils.EnumArmorType;
import net.minecraft.IconRegister;
import net.minecraft.Entity;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityPlayer;
import net.minecraft.ItemArmor;
import net.minecraft.ItemStack;
import net.minecraft.DamageSource;
import net.minecraftforge.common.ISpecialArmor;
import thaumcraft.api.IGoggles;
import thaumcraft.api.nodes.IRevealer;

import java.util.Locale;

@Optional.InterfaceList(value = {@Optional.Interface(iface = "thaumcraft.api.nodes.IRevealer", modid = "Thaumcraft"), @Optional.Interface(iface = "thaumcraft.api.IGoggles", modid = "Thaumcraft")})
public class RMArmor extends ItemArmor implements ISpecialArmor, IRevealer, IGoggles
{
	private final EnumArmorType armorPiece;
	public RMArmor(EnumArmorType armorType)
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextItemID(), PEToolMaterials.RED_MATTER, armorType.ordinal(), false);
		this.setCreativeTab(ObjHandler.cTab);
		this.setUnlocalizedName("pe_rm_armor_" + armorType.ordinal());
						this.armorPiece = armorType;
	}

	public static boolean hasFullSet(EntityPlayer player)
	{
		for (ItemStack i : player.inventory.armorInventory)
		{
			if (i == null || !(i.getItem() instanceof RMArmor))
			{
				return false;
			}
		}
		return true;
	}

	public static boolean hasAnyPiece(EntityPlayer player)
	{
		for (ItemStack i : player.inventory.armorInventory)
		{
			if (i != null && i.getItem() instanceof RMArmor)
			{
				return true;
			}
		}
		return false;
	}

	public EnumArmorType getArmorPiece()
	{
		return this.armorPiece;
	}

	@Override
	public String getArmorType() {
		if (armorPiece == null)
		{
			return "armor";
		}
		switch (armorPiece)
		{
			case HEAD:
				return "helmet";
			case CHEST:
				return "chestplate";
			case LEGS:
				return "leggings";
			default:
				return "boots";
		}
	}

	@Override
	public int getNumComponentsForDurability() {
		switch (this.armorType)
		{
			case 0:
				return 5;
			case 1:
				return 8;
			case 2:
				return 7;
			default:
				return 4;
		}
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) 
	{
		EnumArmorType type = ((RMArmor) armor.getItem()).armorPiece;
		if (source.isExplosion())
		{
			return new ArmorProperties(1, 1.0D, 500);
		}

		if (type == EnumArmorType.FEET && source == DamageSource.fall)
		{
			return new ArmorProperties(1, 1.0D, 10);
		}
		
		if (type == EnumArmorType.HEAD || type == EnumArmorType.FEET)
		{
			return new ArmorProperties(0, 0.2D, 250);
		}
		
		return new ArmorProperties(0, 0.3D, 350);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) 
	{
		EnumArmorType type = ((RMArmor) armor.getItem()).armorPiece;
		return (type == EnumArmorType.HEAD || type == EnumArmorType.FEET) ? 4 : 6;
	}

	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons (IconRegister par1IconRegister)
	{
		String type = this.armorPiece.name.toLowerCase(Locale.ROOT);
		this.itemIcon = par1IconRegister.registerIcon("projecte:rm_armor/" + type);
	}

	@SideOnly(Side.CLIENT)
	public String getArmorTexture (ItemStack stack, Entity entity, int slot, String type)
	{
		char index = this.armorPiece == EnumArmorType.LEGS ? '2' : '1';
		return "projecte:textures/armor/redmatter_"+index+".png";
	}

	@Override
	@Optional.Method(modid = "Thaumcraft")
	public boolean showIngamePopups(ItemStack itemstack, EntityLivingBase player) 
	{
		return ((RMArmor) itemstack.getItem()).armorPiece == EnumArmorType.HEAD;
	}

	@Override
	@Optional.Method(modid = "Thaumcraft")
	public boolean showNodes(ItemStack itemstack, EntityLivingBase player) 
	{
		return ((RMArmor) itemstack.getItem()).armorPiece == EnumArmorType.HEAD;
	}
}
