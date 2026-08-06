package net.minecraftforge.common.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal Forge Configuration replacement using a simple key=value file.
 */
public class Configuration {
    public static final String CATEGORY_GENERAL = "general";

    private final File file;
    private final Map<String, ConfigCategory> categories = new LinkedHashMap<>();
    private boolean changed;

    public Configuration(File file) {
        this.file = file;
    }

    public void load() {
        if (this.file == null || !this.file.exists()) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(this.file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            String currentCategory = CATEGORY_GENERAL;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) {
                    continue;
                }
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    currentCategory = trimmed.substring(1, trimmed.length() - 1);
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                String type = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")
                        ? Property.BOOLEAN
                        : value.matches("-?\\d+") ? Property.INTEGER : value.matches("-?\\d+\\.\\d+") ? Property.DOUBLE : Property.STRING;
                this.getCategory(currentCategory).put(key, new Property(key, value, type));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config " + this.file, e);
        }
    }

    public void save() {
        if (this.file == null) {
            return;
        }
        if (this.file.getParentFile() != null) {
            this.file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(this.file.toPath(), StandardCharsets.UTF_8)) {
            for (ConfigCategory category : this.categories.values()) {
                if (category.getComment() != null) {
                    writer.write("# " + category.getComment().replace("\n", "\n# "));
                    writer.newLine();
                }
                writer.write("[" + category.getName() + "]");
                writer.newLine();
                for (Property property : category.getValues().values()) {
                    if (property.getComment() != null) {
                        writer.write("# " + property.getComment());
                        writer.newLine();
                    }
                    writer.write(property.getName() + "=" + property.getValue());
                    writer.newLine();
                }
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config " + this.file, e);
        }
        this.changed = false;
    }

    public boolean hasChanged() {
        return this.changed;
    }

    public ConfigCategory getCategory(String category) {
        return this.categories.computeIfAbsent(category, ConfigCategory::new);
    }

    public Property get(String category, String key, String defaultValue, String comment) {
        ConfigCategory configCategory = this.getCategory(category);
        Property property = configCategory.getValues().get(key);
        if (property == null) {
            property = new Property(key, defaultValue, Property.STRING).setComment(comment);
            configCategory.put(key, property);
            this.changed = true;
        } else if (comment != null) {
            // Re-apply the current comment even when the key already exists,
            // so re-saving an existing file regenerates (Chinese) comments
            // that were missing from an older auto-generated file.
            property.setComment(comment);
        }
        return property;
    }

    public boolean getBoolean(String key, String category, boolean defaultValue, String comment) {
        Property property = this.get(category, key, Boolean.toString(defaultValue), comment);
        if (Property.BOOLEAN.equals(property.getType())) {
            return property.getBoolean();
        }
        return defaultValue;
    }

    public int getInt(String key, String category, int defaultValue, int minValue, int maxValue, String comment) {
        Property property = this.get(category, key, Integer.toString(defaultValue), comment);
        try {
            int value = Integer.parseInt(property.getValue());
            return Math.max(minValue, Math.min(maxValue, value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public float getFloat(String key, String category, float defaultValue, float minValue, float maxValue, String comment) {
        Property property = this.get(category, key, Float.toString(defaultValue), comment);
        try {
            float value = Float.parseFloat(property.getValue());
            return Math.max(minValue, Math.min(maxValue, value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getString(String key, String category, String defaultValue, String comment) {
        return this.get(category, key, defaultValue, comment).getValue();
    }

    public String getString(String key, String category, String defaultValue, String comment, String[] allowedValues) {
        String value = this.getString(key, category, defaultValue, comment);
        for (String allowed : allowedValues) {
            if (allowed.equals(value)) {
                return value;
            }
        }
        return defaultValue;
    }
}
