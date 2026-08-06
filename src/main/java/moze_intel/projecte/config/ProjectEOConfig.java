package moze_intel.projecte.config;

import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.items.tools.PEToolMaterials;
import net.minecraft.CraftingManager;
import net.minecraft.IRecipe;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Extra configuration for the MITE port, read from config/projectEO.cfg.
 *
 * When ManyLib is installed it is registered with ManyLib's ConfigManager via
 * reflection (dynamic proxy). The proxy exposes real ManyLib ConfigBoolean /
 * ConfigDouble options and a real DefaultConfigScreen, so the config can be
 * edited from ManyLib's in-game menu; /manylib reload projectEO, entering a
 * world or closing the config screen re-reads/re-writes the .cfg and applies
 * the changes live. When ManyLib is absent the config simply loads at startup
 * - the mod works either way and never hard-depends on malilib.
 */
public final class ProjectEOConfig
{
	public static boolean disableTransmutationTable;
	public static boolean disableCondenser;
	public static float emcExchangeRatio;
	public static boolean enableMatterEnchanting;

	private static File configFile;
	private static final List<IRecipe> removedTransmutationRecipes = new ArrayList<>();
	private static final List<IRecipe> removedCondenserRecipes = new ArrayList<>();
	private static boolean recipesRegistered;

	// ManyLib-backed UI handles (reflection only - no compile dependency).
	private static Object manyLibProxy;
	private static List<?> manyLibValues;
	private static Object manyLibTab;
	private static Object manyLibValueTransmutation;
	private static Object manyLibValueCondenser;
	private static Object manyLibValueEnchanting;
	private static Object manyLibValueRatio;
	// Suppresses the value-change callback while we are syncing values from the
	// .cfg file into the ManyLib widgets (avoids a write-back cascade).
	private static boolean syncingFromFile;

	private ProjectEOConfig()
	{
	}

	/**
	 * Called from PECore.preInit.
	 */
	public static void init(File file)
	{
		configFile = file;
		registerWithManyLibIfPresent();
		load();
	}

	public static void load()
	{
		if (configFile == null)
		{
			return;
		}
		Configuration config = new Configuration(configFile);
		try
		{
			config.load();
			syncingFromFile = true;
			try
			{
			disableTransmutationTable = config.getBoolean("disableTransmutationTable", "general", false,
					"禁用转化桌：开启后不再注册转化桌与便携式转化桌的合成配方。");
			disableCondenser = config.getBoolean("disableCondenser", "general", false,
					"禁用能量凝聚器：开启后不再注册能量凝聚器（MK1 与 MK2）的合成配方。");
			emcExchangeRatio = config.getFloat("emcExchangeRatio", "general", 1.0F, 1.0F, Integer.MAX_VALUE,
					"买与卖emc的比例");
			enableMatterEnchanting = config.getBoolean("enableMatterEnchanting", "general", false,
					"允许为暗物质/红物质/宝石装备附魔，默认关闭。");
			}
			finally
			{
				syncingFromFile = false;
			}

			config.getCategory("general").setComment(
					"ProjectE-MITE 扩展配置。可在游戏内通过 ManyLib 菜单修改，修改后即时生效并写回本文件。");
			updateEnchantabilityFromConfig();
		}
		finally
		{
			config.save();
		}
		syncManyLibValuesFromStatics();
		// CraftingManager may not be initialized yet during preInit; the
		// recipe toggles are applied from markRecipesRegistered() once the
		// recipes actually exist (and again on later /manylib reloads).
		if (recipesRegistered)
		{
			applyRecipeToggles();
			reloadEmiRecipesIfPresent();
		}
	}

	public static void save()
	{
		if (configFile == null)
		{
			return;
		}
		if (manyLibValueTransmutation != null)
		{
			// The ManyLib screen calls save() when it closes; pick up any UI
			// edits and re-apply the enchantability/recipe toggles.
			syncStaticsFromManyLibValues();
			updateEnchantabilityFromConfig();
			applyRecipeToggles();
		}
		writeConfigFile();
	}

	/**
	 * Applies the values currently held by the ManyLib widgets to the static
	 * fields, re-applies the enchantability/recipe toggles and rewrites the
	 * .cfg file (with its Chinese comments).
	 */
	private static void applyFromManyLibValues()
	{
		if (manyLibValueTransmutation == null)
		{
			return;
		}
		syncStaticsFromManyLibValues();
		updateEnchantabilityFromConfig();
		applyRecipeToggles();
		writeConfigFile();
		reloadEmiRecipesIfPresent();
	}

	private static void updateEnchantabilityFromConfig()
	{
		if (enableMatterEnchanting)
		{
			PEToolMaterials.updateEnchantability(60, 80, 100);
		}
		else
		{
			PEToolMaterials.updateEnchantability(0, 0, 0);
		}
	}

	private static void writeConfigFile()
	{
		if (configFile == null)
		{
			return;
		}
		Configuration config = new Configuration(configFile);
		try
		{
			config.load();
			// Write the current static values back into the properties, so the
			// file really persists what the user set (previously this only
			// re-saved the old file, which made every toggle revert).
			config.getCategory("general").setComment(
					"ProjectE-MITE 扩展配置。可在游戏内通过 ManyLib 菜单修改，修改后即时生效并写回本文件。");
			setBooleanProperty(config, "disableTransmutationTable", disableTransmutationTable,
					"禁用转化桌：开启后不再注册转化桌与便携式转化桌的合成配方。");
			setBooleanProperty(config, "disableCondenser", disableCondenser,
					"禁用能量凝聚器：开启后不再注册能量凝聚器（MK1 与 MK2）的合成配方。");
			setFloatProperty(config, "emcExchangeRatio", emcExchangeRatio, "买与卖emc的比例");
			setBooleanProperty(config, "enableMatterEnchanting", enableMatterEnchanting,
					"允许为暗物质/红物质/宝石装备附魔，默认关闭。");
		}
		finally
		{
			config.save();
		}
	}

	private static void setBooleanProperty(Configuration config, String key, boolean value, String comment)
	{
		config.get("general", key, Boolean.toString(value), comment).setValue(Boolean.toString(value));
	}

	private static void setFloatProperty(Configuration config, String key, float value, String comment)
	{
		String text = formatRatio(value);
		config.get("general", key, text, comment).setValue(text);
	}

	/**
	 * Asks EMI to rebuild its recipe cache (only when EMI is installed), so a
	 * recipe removed/re-added by the config toggles disappears/appears in EMI
	 * immediately instead of only after a world reload.
	 */
	private static void reloadEmiRecipesIfPresent()
	{
		try
		{
			Class<?> reloadManager = Class.forName("dev.emi.emi.runtime.EmiReloadManager");
			reloadManager.getMethod("reloadRecipes").invoke(null);
		}
		catch (Throwable noEmi)
		{
			// EMI not installed (or not loaded yet): nothing to refresh.
		}
	}

	/**
	 * Removes the transmutation-table / condenser recipes when their toggle is
	 * on, and re-adds them when it is turned off again. The recipes are always
	 * registered at startup and this is called right after registration, so
	 * both the initial state and later /manylib reloads work. The removed
	 * recipes are kept so they can be restored with their crafting tiers.
	 */
	public static void applyRecipeToggles()
	{
		if (!recipesRegistered)
		{
			return;
		}
		CraftingManager cm = CraftingManager.getInstance();
		if (cm == null)
		{
			return;
		}
		List<IRecipe> list = cm.getRecipeList();

		if (!disableTransmutationTable && !removedTransmutationRecipes.isEmpty())
		{
			list.addAll(removedTransmutationRecipes);
			removedTransmutationRecipes.clear();
		}
		if (!disableCondenser && !removedCondenserRecipes.isEmpty())
		{
			list.addAll(removedCondenserRecipes);
			removedCondenserRecipes.clear();
		}

		if (!disableTransmutationTable && !disableCondenser)
		{
			return;
		}

		Iterator<IRecipe> iter = list.iterator();
		while (iter.hasNext())
		{
			IRecipe recipe = iter.next();
			ItemStack out = recipe.getRecipeOutput();
			if (out == null || out.getItem() == null)
			{
				continue;
			}
			Item item = out.getItem();
			if (disableTransmutationTable
					&& (item == ObjHandler.transmutationTablet
							|| item == net.minecraft.Item.getItem(ObjHandler.transmuteStone)))
			{
				removedTransmutationRecipes.add(recipe);
				iter.remove();
			}
			else if (disableCondenser
					&& (item == net.minecraft.Item.getItem(ObjHandler.condenser)
							|| item == net.minecraft.Item.getItem(ObjHandler.condenserMk2)))
			{
				removedCondenserRecipes.add(recipe);
				iter.remove();
			}
		}
	}

	/**
	 * Called right after ProjectE's recipes are registered, then applies the
	 * current toggles (so an initial "disabled" state takes effect too).
	 */
	public static void markRecipesRegistered()
	{
		recipesRegistered = true;
		applyRecipeToggles();
	}

	/**
	 * Registers this config with ManyLib's ConfigManager through a dynamic
	 * proxy, but only when ManyLib is actually present. The proxy forwards
	 * load()/save()/getName() back to this class, so /manylib reload projectEO
	 * hot-reloads the .cfg, and getConfigScreen()/getValues()/getConfigTabs()
	 * expose real ManyLib options so the in-game config screen actually works
	 * (an empty list would make ManyLib's UI crash/hang). Everything is done
	 * reflectively - there is no compile dependency on malilib.
	 */
	private static void registerWithManyLibIfPresent()
	{
		try
		{
			Class<?> managerClass = Class.forName("fi.dy.masa.malilib.config.ConfigManager");
			Class<?> handlerIface = Class.forName("fi.dy.masa.malilib.config.interfaces.IConfigHandler");
			Class<?> booleanClass = Class.forName("fi.dy.masa.malilib.config.options.ConfigBoolean");
			Class<?> stringClass = Class.forName("fi.dy.masa.malilib.config.options.ConfigString");
			Class<?> tabClass = Class.forName("fi.dy.masa.malilib.config.ConfigTab");
			Class<?> baseClass = Class.forName("fi.dy.masa.malilib.config.options.ConfigBase");
			Class<?> callbackIface = Class.forName("fi.dy.masa.malilib.config.interfaces.IValueChangeCallback");
			Class<?> screenClass = Class.forName("fi.dy.masa.malilib.gui.screen.DefaultConfigScreen");
			Object manager = managerClass.getMethod("getInstance").invoke(null);

			manyLibValueTransmutation = booleanClass.getConstructor(String.class, boolean.class, String.class)
					.newInstance("disableTransmutationTable", false,
							"禁用转化桌：开启后不再注册转化桌与便携式转化桌的合成配方。");
			manyLibValueCondenser = booleanClass.getConstructor(String.class, boolean.class, String.class)
					.newInstance("disableCondenser", false,
							"禁用能量凝聚器：开启后不再注册能量凝聚器（MK1 与 MK2）的合成配方。");
			// Plain text input (no slider): default "1".
			manyLibValueRatio = stringClass.getConstructor(String.class, String.class, String.class)
					.newInstance("emcExchangeRatio", "1", "买与卖emc的比例");
			manyLibValueEnchanting = booleanClass.getConstructor(String.class, boolean.class, String.class)
					.newInstance("enableMatterEnchanting", false,
							"允许为暗物质/红物质/宝石装备附魔，默认关闭。");

			manyLibValues = List.of(manyLibValueTransmutation, manyLibValueCondenser,
					manyLibValueRatio, manyLibValueEnchanting);
			manyLibTab = tabClass.getConstructor(String.class, List.class).newInstance("general", manyLibValues);

			// Value-change callback: toggling an option in the ManyLib UI
			// applies it to the mod and rewrites the .cfg immediately.
			Object callback = Proxy.newProxyInstance(callbackIface.getClassLoader(),
					new Class<?>[]{callbackIface},
					(p, method, args) ->
					{
						if ("onValueChanged".equals(method.getName()) && !syncingFromFile)
						{
							applyFromManyLibValues();
						}
						return null;
					});
			java.lang.reflect.Method setCallback = baseClass.getMethod("setValueChangeCallback", callbackIface);
			for (Object value : manyLibValues)
			{
				setCallback.invoke(value, callback);
			}

			Object proxy = java.lang.reflect.Proxy.newProxyInstance(
					handlerIface.getClassLoader(),
					new Class<?>[]{handlerIface},
					(p, method, args) ->
					{
						String name = method.getName();
						switch (name)
						{
							case "load":
								ProjectEOConfig.load();
								return null;
							case "save":
								ProjectEOConfig.save();
								return null;
							case "getName":
								return "projectEO";
							case "getMenuComment":
								return "ProjectE-MITE 扩展配置（config/projectEO.cfg），可在游戏内修改并即时生效。";
							case "getValues":
							case "getConfigTabs":
								return "getValues".equals(name) ? manyLibValues : List.of(manyLibTab);
							case "getHotkeys":
								return null;
							case "getConfigScreen":
							case "getValueScreen":
								// Refresh from the .cfg (in case it was edited
								// outside the game), then open the real
								// ManyLib config screen for our options.
								ProjectEOConfig.load();
								return screenClass.getConstructor(net.minecraft.GuiScreen.class, handlerIface)
										.newInstance(args[0], p);
						}
						if (method.getDeclaringClass() == Object.class)
						{
							switch (name)
							{
								case "toString":
									return p.getClass().getName();
								case "hashCode":
									return System.identityHashCode(p);
								case "equals":
									return p == args[0];
							}
						}
						return null;
					});
			manyLibProxy = proxy;
			managerClass.getMethod("registerConfig", String.class, handlerIface).invoke(manager, "projectEO", proxy);
		}
		catch (Throwable noManyLib)
		{
			// ManyLib not installed: the config stays static (loaded at
			// startup), which is fine.
		}
	}

	/**
	 * Copies the current static values into the ManyLib widgets (used when
	 * opening the config screen or after a /manylib reload).
	 */
	private static void syncManyLibValuesFromStatics()
	{
		if (manyLibValueTransmutation == null)
		{
			return;
		}
		syncingFromFile = true;
		try
		{
			invokeBoolean(manyLibValueTransmutation, "setBooleanValue", disableTransmutationTable);
			invokeBoolean(manyLibValueCondenser, "setBooleanValue", disableCondenser);
			invokeBoolean(manyLibValueEnchanting, "setBooleanValue", enableMatterEnchanting);
			invokeString(manyLibValueRatio, "setValueFromString", formatRatio(emcExchangeRatio));
		}
		catch (Throwable ignored)
		{
		}
		finally
		{
			syncingFromFile = false;
		}
	}

	/**
	 * Copies the current ManyLib widget values into the static fields (used
	 * when the user closes the config screen or edits a value).
	 */
	private static void syncStaticsFromManyLibValues()
	{
		if (manyLibValueTransmutation == null)
		{
			return;
		}
		try
		{
			disableTransmutationTable = invokeBoolean(manyLibValueTransmutation, "getBooleanValue");
			disableCondenser = invokeBoolean(manyLibValueCondenser, "getBooleanValue");
			enableMatterEnchanting = invokeBoolean(manyLibValueEnchanting, "getBooleanValue");
			String ratioText = invokeString(manyLibValueRatio, "getStringValue");
			try
			{
				emcExchangeRatio = Float.parseFloat(ratioText.trim());
			}
			catch (NumberFormatException ignored)
			{
			}
		}
		catch (Throwable ignored)
		{
		}
	}

	/**
	 * Formats the ratio for the input box: whole numbers stay clean ("1", "8")
	 * instead of "1.0"/"8.0".
	 */
	private static String formatRatio(float ratio)
	{
		if (ratio == Math.round(ratio))
		{
			return String.valueOf((int) ratio);
		}
		return Float.toString(ratio);
	}

	private static boolean invokeBoolean(Object target, String methodName, boolean value) throws Throwable
	{
		return (Boolean) target.getClass().getMethod(methodName, boolean.class).invoke(target, value);
	}

	private static boolean invokeBoolean(Object target, String methodName) throws Throwable
	{
		return (Boolean) target.getClass().getMethod(methodName).invoke(target);
	}

	private static String invokeString(Object target, String methodName, String value) throws Throwable
	{
		return (String) target.getClass().getMethod(methodName, String.class).invoke(target, value);
	}

	private static String invokeString(Object target, String methodName) throws Throwable
	{
		return (String) target.getClass().getMethod(methodName).invoke(target);
	}
}
