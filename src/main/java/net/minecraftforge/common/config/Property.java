package net.minecraftforge.common.config;

public class Property {
    public static final String STRING = "STRING";
    public static final String INTEGER = "INTEGER";
    public static final String DOUBLE = "DOUBLE";
    public static final String BOOLEAN = "BOOLEAN";

    private final String name;
    private String value;
    private final String type;
    private String comment;
    private String minValue;
    private String maxValue;

    public Property(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return this.type;
    }

    public String getComment() {
        return this.comment;
    }

    public Property setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public int getInt() {
        try {
            return Integer.parseInt(this.value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public float getFloat() {
        try {
            return Float.parseFloat(this.value);
        } catch (NumberFormatException e) {
            return 0.0F;
        }
    }

    public boolean getBoolean() {
        return Boolean.parseBoolean(this.value);
    }
}
