package moze_intel.projecte.gameObjs.items.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.ItemMode;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.ParticlePKT;
import moze_intel.projecte.utils.Coordinates;
import moze_intel.projecte.utils.ItemHelper;
import moze_intel.projecte.utils.MathUtils;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.Block;
import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockFarmland;
import net.minecraft.ITileEntityProvider;
import net.minecraft.Material;
import net.minecraft.IconRegister;
import net.minecraft.Enchantment;
import net.minecraft.EnchantmentHelper;
import net.minecraft.Entity;
import net.minecraft.EntityAgeable;
import net.minecraft.EntityList;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityItem;
import net.minecraft.IMob;
import net.minecraft.EntitySheep;
import net.minecraft.EntityPlayer;
import net.minecraft.IDamageableItem;
import net.minecraft.ServerPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.ItemStack;
import net.minecraft.StatList;
import net.minecraft.AxisAlignedBB;
import net.minecraft.DamageSource;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.AttributeModifier;
import net.minecraft.MathHelper;
import net.minecraft.MovingObjectPosition;
import net.minecraft.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class PEToolBase extends ItemMode implements IDamageableItem
{
	public static final float HAMMER_BASE_ATTACK = 13.0F;
	public static final float DARKSWORD_BASE_ATTACK = 12.0F;
	public static final float REDSWORD_BASE_ATTACK = 16.0F;
	public static final float STAR_BASE_ATTACK = 20.0F;
	public static final float KATAR_BASE_ATTACK = 23.0F;

	/** Special mining speed used for the near-unbreakable dark/red matter blocks. */
	public static final float MATTER_BLOCK_SPEED = 1000000000.0F;

	protected String pePrimaryToolClass;
	protected String peToolMaterial;
	protected Set<Material> harvestMaterials;
	protected Set<Block> harvestBlocks;
	protected Set<String> secondaryClasses;

	protected Material toolMaterial;
	protected float materialHarvestEfficiency;
	protected int materialHarvestLevel;
	protected boolean isRedMatter;

	public PEToolBase(String unlocalName, byte numCharge, String[] modeDescrp)
	{
		super(unlocalName, numCharge, modeDescrp);
		harvestMaterials = Sets.newHashSet();
		harvestBlocks = Sets.newHashSet();
		secondaryClasses = Sets.newHashSet();

		this.isRedMatter = unlocalName.startsWith("rm_");
		this.peToolMaterial = isRedMatter ? "rm_tools" : "dm_tools";
		this.toolMaterial = PEToolMaterials.get(isRedMatter);
		this.materialHarvestEfficiency = PEToolMaterials.getHarvestEfficiency(isRedMatter);
		this.materialHarvestLevel = PEToolMaterials.getHarvestLevel(isRedMatter);

		this.setMaterial(new Material[] {toolMaterial});
		this.setMaxDamage(getMultipliedDurability());
		this.setReachBonus(0.75F);
	}

	/* ------------------------------------------------------------------ */
	/* MITE Material-driven tool mechanics                                */
	/* ------------------------------------------------------------------ */

	/**
	 * MITE's material-based durability: base durability (4.0) * number of
	 * components * material durability * 100, mirroring ItemTool.
	 */
	public int getMultipliedDurability()
	{
		return (int) (4.0F * getNumComponentsForDurability() * PEToolMaterials.getDurability(isRedMatter) * 100.0F);
	}

	/**
	 * Mirrors MITE ItemTool: a tool is effective against a block when the
	 * block is made of one of the tool's materials (or is an explicitly listed
	 * block) and the tool's material harvest level is high enough.
	 */
	public boolean isEffectiveAgainstBlock(Block block, int metadata)
	{
		if (block == null)
		{
			return false;
		}
		if (harvestBlocks.contains(block))
		{
			return true;
		}
		if (harvestMaterials.contains(block.blockMaterial))
		{
			return getMaterialHarvestLevel() >= block.getMinHarvestLevel(metadata);
		}
		return false;
	}

	public boolean canHarvestBlock(Block block, ItemStack stack)
	{
		return block != null && (harvestMaterials.contains(block.blockMaterial) || harvestBlocks.contains(block));
	}

	/**
	 * Base harvest efficiency before the material multiplier (MITE ItemTool
	 * uses 4.0 for most tools). Tools may override this for special blocks.
	 */
	public float getBaseHarvestEfficiency(Block block)
	{
		return 4.0F;
	}

	public int getMaterialHarvestLevel()
	{
		return materialHarvestLevel;
	}

	/**
	 * Whether this tool is a mining weapon (hammer, pickaxe or the red matter
	 * morning star). MITE's earth/bedrock elementals are only damageable by
	 * pickaxes and hammers, so this is used by the elemental mixin to decide
	 * whether a ProjectE tool may damage them.
	 */
	public String getPrimaryToolClass()
	{
		return pePrimaryToolClass;
	}

	/**
	 * Whether this weapon gains +0.5 melee attack reach per charge level
	 * (sword / hammer / katar / morning star). Reach only affects normal melee
	 * attacks; the C-key special attack uses its own charge-based AOE radius
	 * (attackAOE) and is intentionally not affected.
	 */
	/**
	 * Whether this tool gains +1 block mining reach per charge level (pickaxe /
	 * hammer / morning star), applied when the targeted block is one the tool is
	 * effective against. Left-click mining reach only; the right-click AOE dig
	 * uses its own charge-based box and is not affected.
	 */
	public boolean gainsChargeMiningReachBonus()
	{
		return "pickaxe".equals(pePrimaryToolClass)
			|| "hammer".equals(pePrimaryToolClass)
			|| "morning_star".equals(pePrimaryToolClass);
	}

	public boolean gainsChargeReachBonus()
	{
		return "sword".equals(pePrimaryToolClass)
			|| "hammer".equals(pePrimaryToolClass)
			|| "katar".equals(pePrimaryToolClass)
			|| "morning_star".equals(pePrimaryToolClass);
	}

	public boolean isMiningWeapon()
	{
		return "hammer".equals(pePrimaryToolClass)
				|| "pickaxe".equals(pePrimaryToolClass)
				|| "morning_star".equals(pePrimaryToolClass);
	}

	/**
	 * Whether this tool may break bedrock/mantle/core with its AOE dig.
	 * Only the red matter morning star's right-click AOE may break bedrock;
	 * dark/red hammers and the morning star cannot break mantle/core.
	 */
	public boolean canBreakUnbreakable(net.minecraft.Block block, int metadata)
	{
		return false;
	}
	/**
	 * True when the tool's current G-key mode is the EMC-conversion mode:
	 * mined blocks that have an EMC value are converted straight into the
	 * player's transmutation EMC instead of dropping; blocks without EMC keep
	 * their normal drops. Subclasses with modes override this.
	 */
	public boolean isEmcMode(ItemStack stack)
	{
		return false;
	}

	/**
	 * True for bedrock and the mantle/core block (hardness is -1, min harvest
	 * level is 100, so the normal harvest checks never accept them).
	 */
	public static boolean isUnbreakableMatterBlock(net.minecraft.Block block, int metadata)
	{
		if (block == null)
		{
			return false;
		}
		return block == net.minecraft.Block.bedrock
				|| block instanceof net.minecraft.BlockMantleOrCore;
	}

	/**
	 * Whether the AOE dig (right-click flat layer or left-click mode AOE) may
	 * remove this block. Bedrock/mantle/core are governed solely by
	 * canBreakUnbreakable - BeyondExtreme raises their hardness from -1 to
	 * 100, so the -1-hardness check alone would let dark/red hammers break
	 * them.
	 */
	public boolean canAoeDigBlock(net.minecraft.Block block, int metadata)
	{
		if (isUnbreakableMatterBlock(block, metadata))
		{
			return canBreakUnbreakable(block, metadata);
		}
		return block != null && block.getBlockHardness(metadata) != -1;
	}

	@Override
	public float getStrVsBlock(Block block, int metadata)
	{
		if (!isEffectiveAgainstBlock(block, metadata))
		{
			return 0.0F;
		}
		if (isUnbreakableMatterBlock(block, metadata) && !canBreakUnbreakable(block, metadata))
		{
			return 0.0F;
		}
		return getBaseHarvestEfficiency(block) * materialHarvestEfficiency;
	}

	public int getHarvestLevel(ItemStack stack, String toolClass)
	{
		if (this.pePrimaryToolClass.equals(toolClass) || this.secondaryClasses.contains(toolClass))
		{
			return materialHarvestLevel;
		}
		return -1;
	}

	@Override
	public int getItemEnchantability()
	{
		return PEToolMaterials.getEnchantability(isRedMatter);
	}

	/**
	 * Base attack damage of this tool (mirrors the original ProjectE
	 * attribute values). Weapons override this; utility tools return 0.
	 */
	public float getAttackDamage()
	{
		return 0.0F;
	}

	/**
	 * MITE-ITE redirects ItemStack.getAttributeModifiers() to a method that
	 * only ItemTool subclasses implement, so ProjectE tools (which extend
	 * ItemMode) never got their attack damage shown in the Shift tooltip.
	 * Show it directly here, mirroring the vanilla attribute line.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean par4, net.minecraft.Slot slot)
	{
		super.addInformation(stack, player, list, par4, slot);
		if (!par4)
		{
			return;
		}
		// Attack damage is NOT added here manually: it is fed through the
		// attribute system (getAttrModifiers / getItemAttributeModifiers) so
		// both the Shift tooltip and the actual left-click damage use the same
		// value. Adding a line here duplicates the attribute line that MITE's
		// ItemStack.getTooltip renders on its own.
		// Dynamic MITE-ITE tool level / exp display: only when ITE is loaded,
		// and only for stacks that actually carry upgrade data.
		if (hasExpAndLevel() && stack != null && stack.hasTagCompound()
				&& stack.stackTagCompound.hasKey("tool_level"))
		{
			int level = getToolLevel(stack);
			int maxLevel = getMaxToolLevel(stack);
			if (maxLevel > 0)
			{
				list.add(net.minecraft.EnumChatFormatting.GRAY
						+ net.minecraft.StatCollector.translateToLocal("miteite.tool.modifier.level")
						+ " " + level + "/" + maxLevel);
			}
			if (stack.stackTagCompound.hasKey("tool_exp"))
			{
				list.add(net.minecraft.EnumChatFormatting.GRAY
						+ net.minecraft.StatCollector.translateToLocal("miteite.tool.modifier.exp")
						+ " " + net.minecraft.EnumChatFormatting.WHITE
						+ stack.stackTagCompound.getInteger("tool_exp"));
			}
		}
		addIteModifiersToTooltip(stack, list);
		// Dynamic Extreme gem display: only when Extreme is loaded and the
		// stack actually carries a gem list.
		if (stack != null && stack.hasTagCompound() && stack.stackTagCompound.hasKey("Gems"))
		{
			Float gemDamage = getGemMaxNumericReflect(stack, "damage");
			if (gemDamage != null && gemDamage > 0.0F)
			{
				list.add("" + net.minecraft.EnumChatFormatting.DARK_PURPLE
						+ net.minecraft.EnumChatFormatting.LIGHT_PURPLE + "宝石: +"
						+ net.minecraft.ItemStack.field_111284_a.format((double) gemDamage));
			}
		}
	}

	/* ------------------------------------------------------------------ */
	/* MITE-ITE upgradable item integration (dynamic, no hard dependency)  */
	/* ------------------------------------------------------------------ */

	/**
	 * Mirrors MITE-ITE's IUpgradableItem methods. When ITE is installed these
	 * override the mixin-injected Item methods, so ProjectE tools gain tool
	 * level/exp like any vanilla tool; without ITE they are inert extra
	 * methods on the subclass.
	 */
	public boolean hasExpAndLevel()
	{
		return true;
	}

	/**
	 * Mirrors MITE-ITE's Item.isWeapon(Item) hook. ITE only awards tool
	 * experience in its melee-attack handler when the held item reports
	 * itself as a weapon; the ITE-injected default returns false for anything
	 * that is not an ItemTool, so ProjectE weapons (sword/katar/hammer/...)
	 * never earned experience from their special attacks. Treat every tool
	 * with an attack damage value as a weapon - without ITE this is just an
	 * extra inert method.
	 */
	public boolean isWeapon(net.minecraft.Item item)
	{
		return getAttackDamage() > 0.0F;
	}

	public int getToolLevel(ItemStack stack)
	{
		return stack != null && stack.stackTagCompound != null
				? stack.stackTagCompound.getInteger("tool_level") : 0;
	}

	public int getMaxToolLevel(ItemStack stack)
	{
		int forging = stack != null && stack.stackTagCompound != null
				? stack.stackTagCompound.getInteger("forging_grade") : 0;
		return getMaterialHarvestLevel() * 3 + forging;
	}

	public boolean isMaxToolLevel(ItemStack stack)
	{
		return getToolLevel(stack) >= getMaxToolLevel(stack);
	}

	public int getExpReqForLevel(int level, boolean is_weapon)
	{
		return (getMaterialHarvestLevel() * 8 + 40) * (level + 1) * (is_weapon ? 2 : 1);
	}

	public float getEquipmentExpBounce(ItemStack stack)
	{
		return 0.0F;
	}

	/**
	 * MITE-ITE injects onItemLevelUp into Item as a NO-OP - the real modifier
	 * roll lives in ItemToolTrans (ItemTool only). Non-ItemTool items that use
	 * ITE's tool level system must implement it themselves, otherwise they
	 * level up forever without ever rolling a modifier. Mirror ITE here: pick
	 * a random applicable tool modifier and write/increment it in the
	 * "modifiers" compound. Without ITE this method is inert.
	 */
	public void onItemLevelUp(net.minecraft.NBTTagCompound nbt, net.minecraft.EntityPlayer player, net.minecraft.ItemStack stack)
	{
		try
		{
			Class<?> modifierUtils = Class.forName("net.xiaoyu233.mitemod.miteite.item.ModifierUtils");
			Object applicable = modifierUtils.getMethod("getAllCanBeAppliedToolModifiers", net.minecraft.ItemStack.class)
					.invoke(null, stack);
			Object chosen = modifierUtils.getMethod("getModifierWithWeight",
					java.util.List.class, java.util.Random.class).invoke(null, applicable, player.getRNG());
			if (chosen == null || nbt == null)
			{
				return;
			}
			if (!nbt.hasKey("modifiers"))
			{
				nbt.setCompoundTag("modifiers", new net.minecraft.NBTTagCompound());
			}
			net.minecraft.NBTTagCompound modifiers = nbt.getCompoundTag("modifiers");
			Class<?> toolModifierTypes = Class.forName("net.xiaoyu233.mitemod.miteite.item.ToolModifierTypes");
			String nbtName = (String) toolModifierTypes.getField("nbtName").get(chosen);
			modifiers.setInteger(nbtName, modifiers.getInteger(nbtName) + 1);
		}
		catch (Throwable t)
		{
			// ITE absent or its API changed - the level display still works,
			// the modifier roll is simply skipped.
		}
	}

	/**
	 * Displays the MITE-ITE tool modifiers currently rolled on the stack
	 * (e.g. damage/sharpness, slowdown, demon power) in the Shift tooltip,
	 * mirroring ITE's ItemTool addInformation. Inert without ITE.
	 */
	private static void addIteModifiersToTooltip(net.minecraft.ItemStack stack, java.util.List list)
	{
		if (stack == null || stack.stackTagCompound == null || !stack.stackTagCompound.hasKey("modifiers"))
		{
			return;
		}
		try
		{
			Class<?> toolModifierTypes = Class.forName("net.xiaoyu233.mitemod.miteite.item.ToolModifierTypes");
			Object[] values = (Object[]) toolModifierTypes.getMethod("values").invoke(null);
			net.minecraft.NBTTagCompound modifiers = stack.stackTagCompound.getCompoundTag("modifiers");
			if (modifiers.hasNoTags())
			{
				return;
			}
			// Mirror ITE's ItemTool addInformation: header line, then each
			// rolled modifier as "colour + display name + roman level".
			list.add(net.minecraft.I18n.getString("miteite.tool.modifier.modifiers"));
			java.lang.reflect.Method toRoman = Class.forName("net.xiaoyu233.mitemod.miteite.util.StringUtil")
					.getMethod("intToRoman", int.class);
			for (Object modifier : values)
			{
				String nbtName = (String) toolModifierTypes.getField("nbtName").get(modifier);
				if (!modifiers.hasKey(nbtName))
				{
					continue;
				}
				int level = modifiers.getInteger(nbtName);
				Object display = toolModifierTypes.getMethod("getDisplayName").invoke(modifier);
				String name = display instanceof net.minecraft.ChatMessageComponent
						? ((net.minecraft.ChatMessageComponent) display).toString() : nbtName;
				String roman = String.valueOf(toRoman.invoke(null, level));
				Object color = toolModifierTypes.getField("color").get(modifier);
				list.add("" + (color instanceof net.minecraft.EnumChatFormatting
						? (net.minecraft.EnumChatFormatting) color : net.minecraft.EnumChatFormatting.DARK_AQUA)
						+ name + roman);
			}
		}
		catch (Throwable t)
		{
		}
	}

	/**
	 * Reads the Extreme gem bonus for the given GemModifierTypes name via
	 * reflection (IEXItemStack.getGemMaxNumeric). Returns null when Extreme
	 * is not installed or the call fails, so the caller can skip quietly.
	 */
	@SideOnly(Side.CLIENT)
	private static Float getGemMaxNumericReflect(ItemStack stack, String gemTypeName)
	{
		try
		{
			Class<?> gemTypeClass = Class.forName("cn.wensc.mitemod.extreme.item.GemModifierTypes");
			Object gemType = Enum.valueOf((Class<? extends Enum>) gemTypeClass, gemTypeName);
			java.lang.reflect.Method m = net.minecraft.ItemStack.class.getMethod("getGemMaxNumeric", gemTypeClass);
			return (Float) m.invoke(stack, gemType);
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	/**
	 * Whether the tool exposes its attack damage through the attribute system.
	 * Weapons mirror the original ProjectE behaviour and hide the attribute
	 * damage when the legacy "useOldDamage" config is enabled.
	 */
	public boolean hasAttributeDamage()
	{
		return true;
	}

	@Override
	public float getMeleeDamageBonus()
	{
		return getAttackDamage();
	}

	@Override
	public Multimap getItemAttributeModifiers()
	{
		Multimap multimap = super.getItemAttributeModifiers();
		float damage = getAttackDamage();
		if (damage > 0.0F && hasAttributeDamage())
		{
			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
					new AttributeModifier(field_111210_e, "Weapon modifier", damage, 0));
		}
		return multimap;
	}

	public Multimap getAttributeModifiers(ItemStack stack)
	{
		Multimap multimap = com.google.common.collect.HashMultimap.create();
		float damage = getAttackDamage();
		if (damage > 0.0F && hasAttributeDamage())
		{
			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
					new AttributeModifier(field_111210_e, "Weapon modifier", damage, 0));
		}
		return multimap;
	}

	/**
	 * MITE-ITE injects Item.getAttrModifiers(ItemStack) and uses it to feed
	 * the player's SharedMonsterAttributes.attackDamage while the item is
	 * held - this is what actually makes vanilla tools deal their listed
	 * melee damage on left click. ProjectE tools never implemented it (only
	 * ItemTool subclasses get ITE's implementation), so their left-click
	 * damage fell back to the bare-fist attribute value instead of the
	 * displayed attack damage. Without ITE this is an inert extra method.
	 */
	public Multimap getAttrModifiers(ItemStack stack)
	{
		Multimap multimap = com.google.common.collect.HashMultimap.create();
		float damage = getAttackDamage() + getIteDamageModifier(stack);
		if (damage > 0.0F && hasAttributeDamage())
		{
			multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
					new AttributeModifier(field_111210_e, "Weapon modifier", damage, 0));
		}
		return multimap;
	}

	/**
	 * Reads a MITE-ITE tool modifier value (e.g. the random "damage" or
	 * "demon" modifier granted on level-up) from the stack NBT via
	 * reflection. Returns 0 when ITE is not installed or the stack carries no
	 * such modifier, so the weapon keeps its listed attack damage and gains
	 * the level-up bonus on top once ITE's tool system has written it.
	 */
	private static float getIteModifierValue(ItemStack stack, String fieldName)
	{
		if (stack == null || stack.stackTagCompound == null)
		{
			return 0.0F;
		}
		try
		{
			Class<?> typeClass = Class.forName("net.xiaoyu233.mitemod.miteite.item.ToolModifierTypes");
			Object modifier = typeClass.getField(fieldName).get(null);
			java.lang.reflect.Method getValue = typeClass.getMethod("getModifierValue", net.minecraft.NBTTagCompound.class);
			Object value = getValue.invoke(modifier, stack.stackTagCompound);
			return value instanceof Number ? ((Number) value).floatValue() : 0.0F;
		}
		catch (Throwable t)
		{
			return 0.0F;
		}
	}

	private static float getIteDamageModifier(ItemStack stack)
	{
		return getIteModifierValue(stack, "DAMAGE_MODIFIER");
	}



	/* ------------------------------------------------------------------ */
	/* MITE durability / decay                                            */
	/* ------------------------------------------------------------------ */

	@Override
	public int getNumComponentsForDurability()
	{
		return 3;
	}

	@Override
	public int getRepairCost()
	{
		return getNumComponentsForDurability() * 2;
	}

	/**
	 * ProjectE tools cannot be repaired on an anvil (they never take real
	 * durability damage; the "damage" field is reused to display charge).
	 */
	@Override
	public net.minecraft.Item getRepairItem()
	{
		return null;
	}

	/**
	 * The charge is displayed through MITE's native durability bar, so the
	 * item damage field encodes the charge as damage = maxDamage - charge.
	 * Max durability is 1 + numCharges and the charge range is 1 ..
	 * numCharges + 1, so the bar fills from 1/(numCharges+1) at the minimum
	 * charge up to a full bar at max charge (MITE hides a full bar, which
	 * reads as "topped out"). Dark matter tools (2 charges) go
	 * 1/3 -> 2/3 -> 3/3 full, red matter tools (3 charges) go
	 * 1/4 -> 2/4 -> 3/4 -> 4/4 full.
	 */
	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return getNumCharges() + 1;
	}

	/**
	 * ProjectE tools never take durability damage - the damage field is the
	 * charge display, not real wear. MITE routes all external item damage
	 * (attacks, fire/lava, slime acid corrosion, ...) through
	 * ItemStack.tryDamageItem, which consults isHarmedBy; returning false
	 * makes the tool immune to every source, so a slime's acid can no longer
	 * corrupt the charge display or destroy the tool.
	 */
	@Override
	public boolean isHarmedBy(DamageSource source)
	{
		return false;
	}

	/**
	 * MITE melee "hand damage" counterattack (EntityLiving.onMeleeAttacked):
	 * a melee-attacked mob may hit the attacker back - gelatinous cubes 100%
	 * with their attack-strength multiplier, fire elementals 100% with 1,
	 * normal mobs 12.5% with 1 - UNLESS the attacker holds an item that
	 * prevents hand damage (vanilla: instanceof ItemTool, or stick/bone).
	 * ProjectE tools extend ItemMode instead of ItemTool, so without this
	 * override every melee hit (including the C-key special attack) was
	 * treated as a bare-hand hit and slimes reflected damage back.
	 */
	@Override
	public boolean preventsHandDamage()
	{
		return true;
	}

	@Override
	public byte getCharge(ItemStack stack)
	{
		if (stack == null)
		{
			return 0;
		}
		int damage = stack.getItemDamage();
		if (damage < 0)
		{
			// Brand new / uninitialised stack: charge starts at the minimum level.
			return 1;
		}
		// damage 0 means fully charged (a full durability bar is hidden by
		// MITE's renderer, so max charge reads as "topped out").
		return (byte) Math.max(1, Math.min(getNumCharges() + 1, getNumCharges() + 1 - damage));
	}

	@Override
	public void changeCharge(EntityPlayer player, ItemStack stack)
	{
		byte currentCharge = getCharge(stack);

		if (player.isSneaking())
		{
			if (currentCharge > 1)
			{
				player.worldObj.playSoundAtEntity(player, "projecte:item.peuncharge", 1.0F, 0.5F + ((0.5F / (float) getNumCharges()) * currentCharge));
				stack.setItemDamage(getNumCharges() + 1 - (currentCharge - 1));
			}
		}
		else if (currentCharge < getNumCharges() + 1)
		{
			player.worldObj.playSoundAtEntity(player, "projecte:item.pecharge", 1.0F, 0.5F + ((0.5F / (float) getNumCharges()) * currentCharge));
			stack.setItemDamage(getNumCharges() + 1 - (currentCharge + 1));
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5)
	{
		super.onUpdate(stack, world, entity, par4, par5);
		// Initialise the charge display for fresh stacks (default = minimum
		// charge 1). damage 0 is also the fully-charged state, so only do this
		// once, tracked by the NBT flag - otherwise a max-charged tool would
		// be reset back to minimum charge every tick.
		if (stack.getItemDamage() == 0 && stack.getTagCompound() != null && !stack.getTagCompound().getBoolean("PEInit"))
		{
			stack.setItemDamage(getNumCharges());
			stack.getTagCompound().setBoolean("PEInit", true);
		}
	}

	/**
	 * MITE hook called when a block is actually harvested (server side).
	 * Dark/red matter tools never lose durability, matching the original
	 * ProjectE behaviour. Subclasses may override
	 * {@link #onToolBlockDestroyed(BlockBreakInfo)} to add ProjectE behaviour
	 * such as mode-based AOE digging.
	 */
	@Override
	public boolean onBlockDestroyed(BlockBreakInfo info)
	{
		onToolBlockDestroyed(info);
		return false;
	}

	protected void onToolBlockDestroyed(BlockBreakInfo info)
	{
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public void registerIcons(IconRegister register)
	{
		this.itemIcon = register.registerIcon(this.getTexture(peToolMaterial, pePrimaryToolClass));
	}

	/**
	 * Clears the given OD name in an AOE. Charge affects the AOE. Optional per-block EMC cost.
	 */
	protected void clearOdAOE(World world, ItemStack stack, EntityPlayer player, String odName, int emcCost)
	{
		byte charge = getCharge(stack);
		if (charge == 0 || world.isRemote || ProjectEConfig.disableAllRadiusMining)
		{
			return;
		}
		clearOdAOERadius(world, stack, player, odName, emcCost, 5 * charge, 10 * charge);
	}

	/**
	 * Clears the given OD name within an explicit radius (works from charge 0,
	 * used by the red matter katar). Optional per-block EMC cost.
	 */
	protected void clearOdAOERadius(World world, ItemStack stack, EntityPlayer player, String odName, int emcCost, int radiusX, int radiusY)
	{
		if (world.isRemote || ProjectEConfig.disableAllRadiusMining)
		{
			return;
		}
		List<ItemStack> drops = Lists.newArrayList();

		for (int x = (int) player.posX - radiusX; x <= player.posX + radiusX; x++)
			for (int y = (int) player.posY - radiusY; y <= player.posY + radiusY; y++)
				for (int z = (int) player.posZ - radiusX; z <= player.posZ + radiusX; z++)
				{
					Block block = world.getBlock(x, y, z);

					if (block == Blocks.air)
					{
						continue;
					}

					ItemStack s = new ItemStack(block);
					int[] oreIds = OreDictionary.getOreIDs(s);

					String oreName;
					if (oreIds.length == 0)
					{
						if (block == Blocks.brown_mushroom_block || block == Blocks.red_mushroom_block)
						{
							oreName = "logWood";
						}
						else
						{
							continue;
						}
					}
					else {
						oreName = OreDictionary.getOreName(oreIds[0]);
					}

					if (odName.equals(oreName))
					{
						ArrayList<ItemStack> blockDrops = WorldHelper.getBlockDrops(world, player, block, stack, x, y, z);

						if (PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z)
							&& consumeFuel(player, stack, emcCost, true))
						{
							drops.addAll(blockDrops);
							world.setBlockToAir(x, y, z);
							if (world.rand.nextInt(5) == 0)
							{
								PacketHandler.sendToAllAround(new ParticlePKT("largesmoke", x, y, z), new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y + 1, z, 32));
							}
						}
					}
				}

		WorldHelper.createLootDrop(drops, world, player.posX, player.posY, player.posZ);
		PlayerHelper.swingItem(player);
	}

	/**
	 * Tills in an AOE. Charge affects the AOE. Optional per-block EMC cost.
	 */
	protected void tillAOE(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int meta, int emcCost)
	{
		byte charge = this.getCharge(stack);
		// Charge 1 (the minimum) tills exactly the clicked block; each further
		// charge level widens the square by one block on every side.
		int radius = Math.max(0, charge - 1);
		boolean hasAction = false;
		boolean hasSoundPlayed = false;

		for (int i = x - radius; i <= x + radius; i++)
		{
			for (int j = z - radius; j <= z + radius; j++)
			{
				Block block = world.getBlock(i, y, j);
				Block blockAbove = world.getBlock(i, y + 1, j);

				// MITE represents air as a null block, so blockAbove is null
				// when tilling ordinary ground; only a non-null solid block
				// above prevents tilling.
				if ((blockAbove == null || !blockAbove.isSolid(0)) && (block == Blocks.grass || block == Blocks.dirt))
				{
					if (!hasSoundPlayed)
					{
						world.playSoundEffect((double)((float)i + 0.5F), (double)((float)y + 0.5F), (double)((float)j + 0.5F), Blocks.farmland.stepSound.getBreakSound(), (Blocks.farmland.stepSound.getVolume() + 1.0F) / 2.0F, Blocks.farmland.stepSound.getPitch() * 0.8F);
						hasSoundPlayed = true;
					}

					if (world.isRemote)
					{
						return;
					}
					else
					{
						if (MinecraftForge.EVENT_BUS.post(new UseHoeEvent(player, stack, world, i, y, j)))
						{
							continue;
						}

						// The initial block we target is always free
						if ((i == x && j == z) || consumeFuel(player, stack, emcCost, true))
						{
							PlayerHelper.checkedReplaceBlock(((ServerPlayer) player), i, y, j, Blocks.farmland, 0);

							// Dark/red matter hoes till fertilized farmland.
							BlockFarmland.setFertilized(world, i, y, j, true);

							if (blockAbove != null)
							{
								if ((blockAbove.blockMaterial == Material.plants || blockAbove.blockMaterial == Material.vine)
										&& !(blockAbove instanceof ITileEntityProvider)
										) {
									if (PlayerHelper.hasBreakPermission(((ServerPlayer) player), i, y + 1, j))
									{
										world.setBlockToAir(i, y + 1, j);
									}
								}
							}

							if (!hasAction)
							{
								hasAction = true;
							}
						}
					}
				}
			}
		}
		if (hasAction)
		{
			player.worldObj.playSoundAtEntity(player, "projecte:item.pecharge", 1.0F, 1.0F);
		}
	}

	/**
	 * Called by multiple tools' left click function. Charge has no effect. Free operation.
	 */
	protected void digBasedOnMode(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase living)
	{
		if (world.isRemote || !(living instanceof EntityPlayer))
		{
			return;
		}

		EntityPlayer player = (EntityPlayer) living;
		byte mode = this.getMode(stack);

		if (this.isEmcMode(stack)) // EMC mode: single-block mining only
		{
			return;
		}

		if (mode == 0) // Standard
		{
			return;
		}

		AxisAlignedBB box;

		// The block was already broken when this hook fires, so a fresh
		// raycast is unreliable (it can hit the block behind the gap, an
		// entity, or nothing at all) - that made the AOE modes fire only
		// intermittently. Anchor the box on the broken block and derive the
		// facing from the player's look direction instead. The box formulas
		// below expect the direction of the face that was hit (the opposite
		// of the player's facing), so the mapping is inverted here.
		ForgeDirection direction;
		float pitch = player.rotationPitch;
		if (pitch > 60.0F)
		{
			direction = ForgeDirection.UP;
		}
		else if (pitch < -60.0F)
		{
			direction = ForgeDirection.DOWN;
		}
		else
		{
			int dir = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			switch (dir)
			{
				case 0: direction = ForgeDirection.NORTH; break; // facing +Z
				case 1: direction = ForgeDirection.EAST; break;  // facing -X
				case 2: direction = ForgeDirection.SOUTH; break; // facing -Z
				default: direction = ForgeDirection.WEST; break; // facing +X
			}
		}

		if (ProjectEConfig.disableAllRadiusMining) {
			box = AxisAlignedBB.getBoundingBox(x, y, z, x, y, z);
		} else if (mode == 1) // 3x Tallshot
		{
			box = AxisAlignedBB.getBoundingBox(x, y - 1, z, x, y + 1, z);
		}
		else if (mode == 2) // 3x Wideshot
		{
			if (direction.offsetX != 0)
			{
				box = AxisAlignedBB.getBoundingBox(x, y, z - 1, x, y, z + 1);
			}
			else if (direction.offsetZ != 0)
			{
				box = AxisAlignedBB.getBoundingBox(x - 1, y, z, x + 1, y, z);
			}
			else
			{
				int dir = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

				if (dir == 0 || dir == 2)
				{
					box = AxisAlignedBB.getBoundingBox(x, y, z - 1, x, y, z + 1);
				}
				else
				{
					box = AxisAlignedBB.getBoundingBox(x - 1, y, z, x + 1, y, z);
				}
			}
		}
		else // 3x Longshot
		{
			if (direction.offsetX == 1)
			{
				box = AxisAlignedBB.getBoundingBox(x - 2, y, z, x, y, z);
			}
			else if (direction.offsetX == - 1)
			{
				box = AxisAlignedBB.getBoundingBox(x, y, z, x + 2, y, z);
			}
			else if (direction.offsetZ == 1)
			{
				box = AxisAlignedBB.getBoundingBox(x, y, z - 2, x, y, z);
			}
			else if (direction.offsetZ == -1)
			{
				box = AxisAlignedBB.getBoundingBox(x, y, z, x, y, z + 2);
			}
			else if (direction.offsetY == 1)
			{
				box = AxisAlignedBB.getBoundingBox(x, y - 2, z, x, y, z);
			}
			else
			{
				box = AxisAlignedBB.getBoundingBox(x, y, z, x, y + 2, z);
			}
		}

		List<ItemStack> drops = Lists.newArrayList();

		for (int i = (int) box.minX; i <= box.maxX; i++)
			for (int j = (int) box.minY; j <= box.maxY; j++)
				for (int k = (int) box.minZ; k <= box.maxZ; k++)
				{
					Block b = world.getBlock(i, j, k);

					if (b != Blocks.air
							&& canAoeDigBlock(b, world.getBlockMetadata(i, j, k))
							&& PlayerHelper.hasBreakPermission(((ServerPlayer) player), i, j, k)
							&& (canHarvestBlock(block, new ItemStack(this)) || ForgeHooks.canToolHarvestBlock(block, world.getBlockMetadata(i, j, k), new ItemStack(this))))
					{
						drops.addAll(getAoeDrops(world, stack, player, b, i, j, k));
						world.setBlockToAir(i, j, k);
					}
				}

		WorldHelper.createLootDrop(drops, world, x, y, z);
	}

	/**
	 * Collects a block's drops for the right-click AOE dig modes. In EMC mode
	 * a block with an EMC value is converted straight into the player's
	 * transmutation EMC (no drops); blocks without EMC drop normally. Left-click
	 * mining never goes through this helper, so it is never affected.
	 */
	public static List<ItemStack> getAoeDrops(World world, ItemStack stack, EntityPlayer player, Block block, int x, int y, int z)
	{
		if (stack != null && stack.getItem() instanceof PEToolBase && ((PEToolBase) stack.getItem()).isEmcMode(stack))
		{
			ItemStack blockStack = new ItemStack(block, 1, world.getBlockMetadata(x, y, z));
			int emc = moze_intel.projecte.utils.EMCHelper.getSellValue(blockStack);
			if (emc > 0)
			{
				// Only update the stored EMC. A full sync() here is unsafe:
				// TransmutationProps is shared between the client and server
				// players (keyed by name), and sync() iterates the knowledge
				// list while the other side may mutate it (CME). The EMC is
				// read fresh when the transmutation GUI is opened.
				moze_intel.projecte.playerData.Transmutation.setEmc(player,
						moze_intel.projecte.playerData.Transmutation.getEmc(player) + emc);
				return new ArrayList<ItemStack>();
			}
		}
		return WorldHelper.getBlockDrops(world, player, block, stack, x, y, z);
	}
	/**
	 * Carves in an AOE. Charge affects the breadth and/or depth of the AOE. Optional per-block EMC cost.
	 */
	protected void digAOE(ItemStack stack, World world, EntityPlayer player, boolean affectDepth, int emcCost)
	{
		if (world.isRemote || this.getCharge(stack) == 0 || ProjectEConfig.disableAllRadiusMining)
		{
			return;
		}

		MovingObjectPosition mop = moze_intel.projecte.compat.PECompatHelper.getMovingObjectPositionFromPlayer(world, player);

		if (mop == null || mop.getEntityHit() != null)
		{
			return;
		}

		AxisAlignedBB box = affectDepth ? WorldHelper.getBroadDeepBox(new Coordinates(mop.blockX, mop.blockY, mop.blockZ), ForgeDirection.getOrientation(mop.sideHit), this.getCharge(stack))
				: WorldHelper.getFaceFlatBox(new Coordinates(mop.blockX, mop.blockY, mop.blockZ), ForgeDirection.getOrientation(mop.sideHit), this.getCharge(stack));

		List<ItemStack> drops = Lists.newArrayList();
		boolean anyBroken = false;

		for (int i = (int) box.minX; i <= box.maxX; i++)
			for (int j = (int) box.minY; j <= box.maxY; j++)
				for (int k = (int) box.minZ; k <= box.maxZ; k++)
				{
					Block b = world.getBlock(i, j, k);

					if (b != Blocks.air
							&& canAoeDigBlock(b, world.getBlockMetadata(i, j, k))
							&& canHarvestBlock(b, stack)
							&& PlayerHelper.hasBreakPermission(((ServerPlayer) player), i, j, k)
							&& consumeFuel(player, stack, emcCost, true)
							)
					{
						drops.addAll(getAoeDrops(world, stack, player, b, i, j, k));
						world.setBlockToAir(i, j, k);
						anyBroken = true;
					}
				}

		WorldHelper.createLootDrop(drops, world, mop.blockX, mop.blockY, mop.blockZ);
		PlayerHelper.swingItem(player);
		if (anyBroken)
		{
			world.playSoundAtEntity(player, "projecte:item.pedestruct", 1.0F, 1.0F);
		}
	}

	/**
	 * Attacks through armor. Charge affects damage. Free operation.
	 */
	protected void attackWithCharge(ItemStack stack, EntityLivingBase damaged, EntityLivingBase damager, float baseDmg)
	{
		if (!(damager instanceof EntityPlayer) || damager.worldObj.isRemote)
		{
			return;
		}

		DamageSource dmg = DamageSource.causePlayerDamage((EntityPlayer) damager);
		byte charge = this.getCharge(stack);
		float totalDmg = baseDmg;

		if (charge > 0)
		{
			
			totalDmg += charge;
		}

		damaged.attackEntityFrom(new net.minecraft.Damage(dmg, totalDmg));
	}

	/**
	 * Attacks in an AOE. Charge affects AOE, not damage (intentional). Optional per-entity EMC cost.
	 */
	protected void attackAOE(ItemStack stack, EntityPlayer player, boolean slayAll, float damage, int emcCost)
	{
		if (player.worldObj.isRemote)
		{
			return;
		}

		byte charge = getCharge(stack);
		float factor = 2.5F * charge;
		AxisAlignedBB aabb = player.boundingBox.expand(factor, factor, factor);
		List<Entity> toAttack = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);
		DamageSource src = DamageSource.causePlayerDamage(player);
		
		for (Entity entity : toAttack)
		{
			if (consumeFuel(player, stack, emcCost, true)) {
				if (entity instanceof IMob)
				{
					net.minecraft.EntityDamageResult result = entity.attackEntityFrom(new net.minecraft.Damage(src, damage));
					// Simulate the vanilla melee-attacked callback so MITE-ITE
					// awards tool experience for special-attack damage too.
					if (result != null && result.entityWasNegativelyAffected())
					{
						entity.onMeleeAttacked(player, result);
					}
				}
				else if (entity instanceof EntityLivingBase && slayAll)
				{
					net.minecraft.EntityDamageResult result = entity.attackEntityFrom(new net.minecraft.Damage(src, damage));
					if (result != null && result.entityWasNegativelyAffected())
					{
						entity.onMeleeAttacked(player, result);
					}
				}
			}
		}
		player.worldObj.playSoundAtEntity(player, "projecte:item.pecharge", 1.0F, 1.0F);
		PlayerHelper.swingItem(player);
	}

	/**
	 * Called when tools that act as shears start breaking a block. Free operation.
	 */
	protected void shearBlock(ItemStack stack, int x, int y, int z, EntityPlayer player)
	{
		if (player.worldObj.isRemote)
		{
			return;
		}

		Block block = player.worldObj.getBlock(x, y, z);

		if (block instanceof IShearable)
		{
			IShearable target = (IShearable) block;

			if (target.isShearable(stack, player.worldObj, x, y, z) && PlayerHelper.hasBreakPermission(((ServerPlayer) player), x, y, z))
			{
				ArrayList<ItemStack> drops = target.onSheared(stack, player.worldObj, x, y, z, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack));
				Random rand = new Random();

				for(ItemStack drop : drops)
				{
					float f = 0.7F;
					double d = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
					double d1 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
					double d2 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
					EntityItem entityitem = new EntityItem(player.worldObj, (double)x + d, (double)y + d1, (double)z + d2, drop);
					entityitem.delayBeforeCanPickup = 10;
					player.worldObj.spawnEntityInWorld(entityitem);
				}

				player.addStat(StatList.mineBlockStatArray[block.blockID], 1);
			}
		}
	}

	/**
	 * Shears a single MITE sheep (server side only): drops 1-3 wool of its
	 * fleece colour, plays the shear sound and marks it sheared.
	 */
	protected void shearSheep(EntitySheep sheep)
	{
		if (sheep.worldObj.isRemote || sheep.getSheared() || sheep.isChild())
		{
			return;
		}
		sheep.setSheared(true);
		int count = 1 + sheep.worldObj.rand.nextInt(3);
		for (int i = 0; i < count; i++)
		{
			ItemStack wool = new ItemStack(net.minecraft.Block.cloth.blockID, 1, sheep.getFleeceColor());
			EntityItem item = sheep.dropItemStack(wool, 1.0F);
			if (item != null)
			{
				item.motionY += sheep.worldObj.rand.nextFloat() * 0.05F;
				item.motionX += (sheep.worldObj.rand.nextFloat() - sheep.worldObj.rand.nextFloat()) * 0.1F;
				item.motionZ += (sheep.worldObj.rand.nextFloat() - sheep.worldObj.rand.nextFloat()) * 0.1F;
			}
		}
		sheep.playSound("mob.sheep.shear", 1.0F, 1.0F);
	}

	/**
	 * Shears all sheep around the player in an AOE. Charge affects the radius.
	 */
	protected void shearSheepAOE(ItemStack stack, EntityPlayer player)
	{
		World world = player.worldObj;
		if (world.isRemote)
		{
			return;
		}
		byte charge = this.getCharge(stack);
		int offset = 2 + charge;
		AxisAlignedBB box = player.boundingBox.expand(offset, offset / 2, offset);
		List<Entity> list = world.getEntitiesWithinAABB(EntitySheep.class, box);
		for (Entity ent : list)
		{
			shearSheep((EntitySheep) ent);
		}
	}

	/**
	 * Shears entities in an AOE. Charge affects AOE. Optional per-entity EMC cost.
	 */
	protected void shearEntityAOE(ItemStack stack, EntityPlayer player, int emcCost)
	{
		World world = player.worldObj;
		if (!world.isRemote)
		{
			byte charge = this.getCharge(stack);

			int offset = ((int) Math.pow(2, 2 + charge));

			AxisAlignedBB bBox = player.boundingBox.expand(offset, offset / 2, offset);
			List<Entity> list = world.getEntitiesWithinAABB(IShearable.class, bBox);

			List<ItemStack> drops = Lists.newArrayList();

			for (Entity ent : list)
			{
				IShearable target = (IShearable) ent;

				if (target.isShearable(stack, ent.worldObj, (int) ent.posX, (int) ent.posY, (int) ent.posZ)
						&& consumeFuel(player, stack, emcCost, true)
						)
				{
					ArrayList<ItemStack> entDrops = target.onSheared(stack, ent.worldObj, (int) ent.posX, (int) ent.posY, (int) ent.posZ, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack));

					if (!entDrops.isEmpty())
					{
						for (ItemStack drop : entDrops)
						{
							drop.stackSize *= 2;
						}

						drops.addAll(entDrops);
					}
				}
				if (Math.random() < 0.01)
				{
					Entity e = EntityList.createEntityByName(EntityList.getEntityString(ent), world);
					e.copyDataFrom(ent, true);
					if (e instanceof EntitySheep)
					{
						((EntitySheep) e).setFleeceColor(MathUtils.randomIntInRange(0, 16));
					}
					if (e instanceof EntityAgeable)
					{
						((EntityAgeable) e).setGrowingAge(-24000);
					}
					world.spawnEntityInWorld(e);
				}
			}

			WorldHelper.createLootDrop(drops, world, player.posX, player.posY, player.posZ);
			PlayerHelper.swingItem(player);
		}
	}

	/**
	 * Scans and harvests an ore vein. This is called already knowing the mop is pointing at an ore or gravel.
	 */
	protected void tryVeinMine(ItemStack stack, EntityPlayer player, MovingObjectPosition mop)
	{
		if (player.worldObj.isRemote || ProjectEConfig.disableAllRadiusMining)
		{
			return;
		}

		AxisAlignedBB aabb = WorldHelper.getBroadDeepBox(new Coordinates(mop.blockX, mop.blockY, mop.blockZ), ForgeDirection.getOrientation(mop.sideHit), getCharge(stack));
		Block target = player.worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
		if (target.getBlockHardness(player.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ)) <= -1 || !(canHarvestBlock(target, stack) || ForgeHooks.canToolHarvestBlock(target, player.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ), stack)))
		{
			return;
		}

		List<ItemStack> drops = Lists.newArrayList();

		for (int i = (int) aabb.minX; i <= aabb.maxX; i++)
		{
			for (int j = (int) aabb.minY; j <= aabb.maxY; j++)
			{
				for (int k = (int) aabb.minZ; k <= aabb.maxZ; k++)
				{
					Block b = player.worldObj.getBlock(i, j, k);
					if (b == target)
					{
						WorldHelper.harvestVein(player.worldObj, player, stack, new Coordinates(i, j, k), b, drops, 0);
					}
				}
			}
		}

		WorldHelper.createLootDrop(drops, player.worldObj, mop.blockX, mop.blockY, mop.blockZ);
		if (!drops.isEmpty())
		{
			player.worldObj.playSoundAtEntity(player, "projecte:item.pedestruct", 1.0F, 1.0F);
		}
	}


	/**
	 * Mines all ore veins in a Box around the player.
	 */
	protected void mineOreVeinsInAOE(ItemStack stack, EntityPlayer player) {
		if (player.worldObj.isRemote || ProjectEConfig.disableAllRadiusMining)
		{
			return;
		}
		int offset = this.getCharge(stack) + 3;
		AxisAlignedBB box = player.boundingBox.expand(offset, offset, offset);
		List<ItemStack> drops = Lists.newArrayList();
		World world = player.worldObj;

		for (int x = (int) box.minX; x <= box.maxX; x++)
			for (int y = (int) box.minY; y <= box.maxY; y++)
				for (int z = (int) box.minZ; z <= box.maxZ; z++)
				{
					Block block = world.getBlock(x, y, z);

					if (block == null) // MITE represents air as null
					{
						continue;
					}

					if (ItemHelper.isOre(block, world.getBlockMetadata(x, y, z)) && block.getBlockHardness(world.getBlockMetadata(x, y, z)) != -1 && (canHarvestBlock(block, new ItemStack(this)) || ForgeHooks.canToolHarvestBlock(block, world.getBlockMetadata(x, y, z), new ItemStack(this))))
					{
						WorldHelper.harvestVein(world, player, stack, new Coordinates(x, y, z), block, drops, 0);
					}
				}

		if (!drops.isEmpty())
		{
			WorldHelper.createLootDrop(drops, world, player.posX, player.posY, player.posZ );
			PlayerHelper.swingItem(player);
		}
	}
}
