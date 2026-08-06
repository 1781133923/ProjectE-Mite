package moze_intel.projecte.gameObjs.items.tools;

import huix.glacier.api.extension.material.GlacierMaterial;
import huix.glacier.api.extension.material.IArmorMaterial;
import huix.glacier.api.extension.material.IEquipmentMaterial;
import huix.glacier.api.extension.material.IRepairableMaterial;
import huix.glacier.api.extension.material.IToolMaterial;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.EnumQuality;
import net.minecraft.Item;
import net.minecraft.Material;

/**
 * Custom MITE tool materials for the ProjectE dark/red matter tools.
 *
 * MITE derives most tool behaviour from the Material:
 *  - durability:  4 * components * material.durability * 100
 *  - enchantability / max quality / damage vs entity come from the material
 *  - harvest efficiency comes from IToolMaterial (RIC)
 *  - harvest level comes from setMinHarvestLevel()
 *
 * The dark matter material sits one tier above adamantium (min harvest level 6),
 * the red matter material one tier above that (min harvest level 7).
 */
public final class PEToolMaterials
{
	public static final Material DARK_MATTER;
	public static final Material RED_MATTER;
	public static final Material GEM_MATTER;

	private static PEEquipmentMaterial darkEquipment;
	private static PEEquipmentMaterial redEquipment;
	private static PEEquipmentMaterial gemEquipment;

	// MITE durability values: wood 0.5, iron 8, ancient metal 16, mithril 64, adamantium 256
	public static final float DARK_MATTER_DURABILITY = 512.0F;
	public static final float RED_MATTER_DURABILITY = 1024.0F;

	// MITE harvest efficiency multipliers: iron 2.0, mithril/diamond 2.5, adamantium 3.0
	public static final float DARK_MATTER_HARVEST_EFFICIENCY = 48.0F;
	public static final float RED_MATTER_HARVEST_EFFICIENCY = 64.0F;

	// adamantium min harvest level is 5
	public static final int DARK_MATTER_HARVEST_LEVEL = 6;
	public static final int RED_MATTER_HARVEST_LEVEL = 7;

	// 0 = cannot be enchanted on the enchantment table.
	public static final int DARK_MATTER_ENCHANTABILITY = 0;
	public static final int RED_MATTER_ENCHANTABILITY = 0;

	// MITE armour protection per material: iron 8, mithril 9, adamantium 10.
	public static final int DARK_MATTER_ARMOR_PROTECTION = 16;
	public static final int RED_MATTER_ARMOR_PROTECTION = 20;
	public static final int GEM_MATTER_ARMOR_PROTECTION = 20;

	static
	{
		DARK_MATTER = create("dark_matter", DARK_MATTER_DURABILITY, DARK_MATTER_ENCHANTABILITY, 8.0F,
				DARK_MATTER_HARVEST_EFFICIENCY, DARK_MATTER_HARVEST_LEVEL, DARK_MATTER_ARMOR_PROTECTION, 0);
		RED_MATTER = create("red_matter", RED_MATTER_DURABILITY, RED_MATTER_ENCHANTABILITY, 12.0F,
				RED_MATTER_HARVEST_EFFICIENCY, RED_MATTER_HARVEST_LEVEL, RED_MATTER_ARMOR_PROTECTION, 1);
		GEM_MATTER = create("gem_matter", RED_MATTER_DURABILITY, 0, 12.0F,
				RED_MATTER_HARVEST_EFFICIENCY, RED_MATTER_HARVEST_LEVEL, GEM_MATTER_ARMOR_PROTECTION, 1);
	}

	private PEToolMaterials()
	{
	}

	public static Material get(boolean red)
	{
		return red ? RED_MATTER : DARK_MATTER;
	}

	public static float getDurability(boolean red)
	{
		return red ? RED_MATTER_DURABILITY : DARK_MATTER_DURABILITY;
	}

	public static float getHarvestEfficiency(boolean red)
	{
		return red ? RED_MATTER_HARVEST_EFFICIENCY : DARK_MATTER_HARVEST_EFFICIENCY;
	}

	public static int getHarvestLevel(boolean red)
	{
		return red ? RED_MATTER_HARVEST_LEVEL : DARK_MATTER_HARVEST_LEVEL;
	}

	public static int getEnchantability(boolean red)
	{
		return red ? RED_MATTER_ENCHANTABILITY : DARK_MATTER_ENCHANTABILITY;
	}

	private static Material create(String name, float durability, int enchantability, float damageVsEntity,
			float harvestEfficiency, int minHarvestLevel, int armorProtection, int matterSubtype)
	{
		PEEquipmentMaterial equipment = new PEEquipmentMaterial(name, durability, enchantability,
				damageVsEntity, harvestEfficiency, armorProtection, matterSubtype);
		if ("dark_matter".equals(name))
		{
			darkEquipment = equipment;
		}
		else if ("red_matter".equals(name))
		{
			redEquipment = equipment;
		}
		else if ("gem_matter".equals(name))
		{
			gemEquipment = equipment;
		}
		PEMaterial material = new PEMaterial(equipment, armorProtection);
		material.setMetal(true);
		material.setMinHarvestLevel(minHarvestLevel);
		return material;
	}

	/**
	 * Called from ProjectEOConfig: when matter enchanting is enabled the
	 * materials get a real enchantability (dark 60 / red 80 / gem 100),
	 * otherwise they stay at 0 (cannot be enchanted).
	 */
	public static void updateEnchantability(int dark, int red, int gem)
	{
		if (darkEquipment != null) darkEquipment.setEnchantability(dark);
		if (redEquipment != null) redEquipment.setEnchantability(red);
		if (gemEquipment != null) gemEquipment.setEnchantability(gem);
	}

	/**
	 * GlacierMaterial with armour support: RIC's ItemArmorMixin checks whether
	 * the effective material itself implements IArmorMaterial to supply the
	 * armour protection value used by MITE's damage formula.
	 */
	private static final class PEMaterial extends GlacierMaterial implements IArmorMaterial
	{
		private final int protection;

		private PEMaterial(IEquipmentMaterial material, int protection)
		{
			super(material);
			this.protection = protection;
		}

		@Override
		public int getProtection()
		{
			return protection;
		}
	}

	private static final class PEEquipmentMaterial implements IEquipmentMaterial, IToolMaterial, IRepairableMaterial, IArmorMaterial
	{
		private final String name;
		private final float durability;
		private int enchantability;
		private final float damageVsEntity;
		private final float harvestEfficiency;
		private final int armorProtection;
		private final int matterSubtype;

		private PEEquipmentMaterial(String name, float durability, int enchantability, float damageVsEntity,
				float harvestEfficiency, int armorProtection, int matterSubtype)
		{
			this.name = name;
			this.durability = durability;
			this.enchantability = enchantability;
			this.damageVsEntity = damageVsEntity;
			this.harvestEfficiency = harvestEfficiency;
			this.armorProtection = armorProtection;
			this.matterSubtype = matterSubtype;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public float getDurability()
		{
			return durability;
		}

		@Override
		public int getEnchantability()
		{
			return enchantability;
		}

		public void setEnchantability(int enchantability)
		{
			this.enchantability = enchantability;
		}

		@Override
		public EnumQuality getMaxQuality()
		{
			// The user wants the armour crafted at normal (average) quality.
			return EnumQuality.average;
		}

		@Override
		public float getDamageVsEntity()
		{
			return damageVsEntity;
		}

		@Override
		public float getHarvestEfficiency()
		{
			return harvestEfficiency;
		}

		@Override
		public int getProtection()
		{
			return armorProtection;
		}

		@Override
		public Item getRepairItem()
		{
			return ObjHandler.matter;
		}
	}
}
