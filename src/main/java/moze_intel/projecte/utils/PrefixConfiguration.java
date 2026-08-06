package moze_intel.projecte.utils;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

public class PrefixConfiguration extends Configuration {
	final protected Configuration inner;
	final protected String prefix;
	public PrefixConfiguration(Configuration inner, String prefix) {
		super(null);
		if (prefix.endsWith(".")) throw new IllegalArgumentException("Prefix is not allowed to end with a dot.");
		this.inner = inner;
		this.prefix = prefix;
	}

	@Override
	public ConfigCategory getCategory(String name) {
		if (name == null || "".equals(name)) return this.inner.getCategory(this.prefix);
		return this.inner.getCategory(this.prefix + "." + name);
	}

	public boolean getBoolean(String key, String category, boolean defaultValue, String comment) {
		return this.inner.getBoolean(key, getCategoryName(category), defaultValue, comment);
	}

	public int getInt(String key, String category, int defaultValue, int minValue, int maxValue, String comment) {
		return this.inner.getInt(key, getCategoryName(category), defaultValue, minValue, maxValue, comment);
	}

	public float getFloat(String key, String category, float defaultValue, float minValue, float maxValue, String comment) {
		return this.inner.getFloat(key, getCategoryName(category), defaultValue, minValue, maxValue, comment);
	}

	private String getCategoryName(String category) {
		if (category == null || "".equals(category)) return this.prefix;
		return this.prefix + "." + category;
	}
}
