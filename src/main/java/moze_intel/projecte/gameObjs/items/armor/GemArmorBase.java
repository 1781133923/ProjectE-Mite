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

public abstract class GemArmorBase extends ItemArmor implements ISpecialArmor
{
	private final EnumArmorType armorPiece;

	public GemArmorBase(EnumArmorType armorType)
	{
		super(net.xiaoyu233.fml.reload.utils.IdUtil.getNextItemID(), PEToolMaterials.GEM_MATTER, armorType.ordinal(), false);
		this.setCreativeTab(ObjHandler.cTab);
		this.setUnlocalizedName("pe_gem_armor_" + armorType.ordinal());
						this.armorPiece = armorType;
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
	/**
	 * ITE armor XP curve override (method only exists at runtime when MITE-ITE
	 * is installed): this set uses the 32 + 16*level curve instead of ITE's
	 * unknown-material default (150 + 75*level). Armor-type multiplier mirrors
	 * ItemArmorTrans (head 2, chest 4, legs 3, feet 1).
	 */
	public int getExpReqForLevel(int level, boolean isWeapon)
	{
		return getExpReqForLevel(level, this.armorType, this);
	}

	/**
	 * ITE tooltip/display and the level-up check both call this 3-arg variant
	 * (the tooltip calls it directly), so override it as well as the 2-arg one.
	 */
	public int getExpReqForLevel(int level, int armorType, ItemArmor itemArmor)
	{
		int multiplier;
		switch (armorType)
		{
			case 0: multiplier = 2; break;
			case 1: multiplier = 4; break;
			case 2: multiplier = 3; break;
			case 3: multiplier = 1; break;
			default: return 64 * level;
		}
		return multiplier * (32 + 16 * level);
	}

	@Override
	public String getTextureFilenamePrefix()
	{
		return "gem";
	}

	public static boolean hasAnyPiece(EntityPlayer player)
	{
		for (ItemStack i : player.inventory.armorInventory)
		{
			if (i != null && i.getItem() instanceof GemArmorBase)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean hasFullSet(EntityPlayer player)
	{
		for (ItemStack i : player.inventory.armorInventory)
		{
			if (i == null || !(i.getItem() instanceof GemArmorBase))
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

	/**
	 * MITE has no Forge onArmorTick hook; the ProjectE tick listener calls this
	 * for every worn gem piece on both client and server.
	 */
	public void onArmorTick(net.minecraft.World world, EntityPlayer player, ItemStack stack)
	{
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot)
	{
		EnumArmorType type = ((GemArmorBase) armor.getItem()).armorPiece;
		if (source.isExplosion())
		{
			return new ArmorProperties(1, 1.0D, 750);
		}

		if (type == EnumArmorType.FEET && source == DamageSource.fall)
		{
			return new ArmorProperties(1, 1.0D, 15);
		}

		if (type == EnumArmorType.HEAD || type == EnumArmorType.FEET)
		{
			return new ArmorProperties(0, 0.2D, 400);
		}

		return new ArmorProperties(0, 0.3D, 500);
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot)
	{
		EnumArmorType type = ((GemArmorBase) armor.getItem()).armorPiece;
		return (type == EnumArmorType.HEAD || type == EnumArmorType.FEET) ? 4 : 6;
	}

	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		String type = this.armorPiece.name.toLowerCase(Locale.ROOT);
		this.itemIcon = par1IconRegister.registerIcon("projecte:gem_armor/" + type);
	}

	@SideOnly(Side.CLIENT)
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		char index = this.armorPiece == EnumArmorType.LEGS ? '2' : '1';
		return "projecte:textures/armor/gem_" + index + ".png";
	}
}
