package net.minecraftforge.common.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigCategory {
    private final String name;
    private final Map<String, Property> values = new LinkedHashMap<>();
    private String comment;

    public ConfigCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public ConfigCategory setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getComment() {
        return this.comment;
    }

    public Map<String, Property> getValues() {
        return this.values;
    }

    public Property put(String key, Property property) {
        return this.values.put(key, property);
    }
}
