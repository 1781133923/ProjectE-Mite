package moze_intel.projecte.gameObjs.items.armor;

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

import java.util.Locale;

public class DMArmor extends ItemArmor implements ISpecialArmor
{
	private final EnumArmorType armorPiece;
	public DMArmor(EnumArmorType armorPiece)
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextItemID(), PEToolMaterials.DARK_MATTER, armorPiece.ordinal(), false);
		this.setCreativeTab(ObjHandler.cTab);
		this.setUnlocalizedName("pe_dm_armor_" + armorPiece.ordinal());
						this.armorPiece = armorPiece;
			}

	public static boolean hasFullSet(EntityPlayer player)
	{
		for (ItemStack i : player.inventory.armorInventory)
		{
			if (i == null || !(i.getItem() instanceof DMArmor))
			{
				return false;
			}
		}
		return true;
	}

	public EnumArmorType getArmorPiece()
	{
		return this.armorPiece;
	}

	@Override
	public String getArmorType() {
		if (armorPiece == null)
		{
			// Called from ItemArmor's constructor before this field is set;
			// the item icon texture is overridden by registerIcons anyway.
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
		// Use the inherited armorType (set before durability is computed).
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
		EnumArmorType type = ((DMArmor) armor.getItem()).armorPiece;
		if (source.isExplosion())
		{
			return new ArmorProperties(1, 1.0D, 350);
		}

		if (type == EnumArmorType.FEET && source == DamageSource.fall)
		{
			return new ArmorProperties(1, 1.0D, 5);
		}

		if (type == EnumArmorType.HEAD || type == EnumArmorType.FEET)
		{
			return new ArmorProperties(0, 0.2D, 100);
		}

		return new ArmorProperties(0, 0.3D, 150);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) 
	{
		EnumArmorType type = ((DMArmor) armor.getItem()).armorPiece;
		return (type == EnumArmorType.HEAD || type == EnumArmorType.FEET) ? 4 : 6;
	}

	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons (IconRegister par1IconRegister)
	{
		String type = this.armorPiece.name.toLowerCase(Locale.ROOT);
		
		this.itemIcon = par1IconRegister.registerIcon("projecte:dm_armor/" + type);
	}

	@SideOnly(Side.CLIENT)
	public String getArmorTexture (ItemStack stack, Entity entity, int slot, String type)
	{
		char index = this.armorPiece == EnumArmorType.LEGS ? '2' : '1';
		return "projecte:textures/armor/darkmatter_"+index+".png";
	}
}
