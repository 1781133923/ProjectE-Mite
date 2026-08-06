package moze_intel.projecte.utils;

import cpw.mods.fml.common.Loader;

import me.towdium.pinin.PinIn;
import net.minecraft.ItemStack;

import java.util.Locale;

	public abstract class ItemSearchHelper
{
	private static PinIn pinIn;
	private static boolean pinInChecked;

	/**
	 * 拼音搜索（基础版）：PinIn-Lib 为可选依赖。注意不能用
	 * Loader.isModLoaded() 判断 - 这个环境的 FML Loader shim 从不注册
	 * 第三方 mod 容器，恒返回 false。改为直接探测类是否在 classpath 上。
	 */
	private static PinIn getPinIn()
	{
		if (!pinInChecked)
		{
			pinInChecked = true;
			try
			{
				Class.forName("me.towdium.pinin.PinIn");
				pinIn = new PinIn();
				PELogger.logInfo("PinIn pinyin search enabled.");
			}
			catch (Throwable t)
			{
				pinIn = null;
				PELogger.logInfo("PinIn pinyin search unavailable (PinIn-Lib not installed).");
			}
		}
		return pinIn;
	}

	public static ItemSearchHelper create(String searchString) {
		if (Loader.isModLoaded("NotEnoughItems")) {
			return new ItemSearchHelperNEI(searchString);
		} else {
			return new DefaultSearch(searchString);
		}
	}

	public final String searchString;
	public ItemSearchHelper(String searchString) {
		this.searchString = searchString;
	}

	public final boolean doesItemMatchFilter(ItemStack itemStack) {
		try {
			return this.doesItemMatchFilter_(itemStack);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	protected abstract boolean doesItemMatchFilter_(ItemStack itemStack);

	private static class DefaultSearch extends ItemSearchHelper
	{
		public DefaultSearch(String searchString)
		{
			super(searchString);
		}

		public boolean doesItemMatchFilter_(ItemStack stack)
		{
			String displayName;

			try
			{
				displayName = stack.getDisplayName().toLowerCase(Locale.ROOT);
			} catch (Exception e)
			{
				e.printStackTrace();
				//From old code... Not sure if intended to not remove items that crash on getDisplayName
				return true;
			}

			if (displayName == null)
			{
				return false;
			}
			else if (searchString.length() > 0 && !displayName.contains(searchString))
			{
				// 中文物品名额外支持拼音/首字母搜索（如 "xiangshu"、"xs" 匹配橡树树苗）。
				try
				{
					PinIn pinin = getPinIn();
					if (pinin == null || !pinin.contains(stack.getDisplayName(), searchString))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					return false;
				}
			}
			return true;
		}
	}
}
