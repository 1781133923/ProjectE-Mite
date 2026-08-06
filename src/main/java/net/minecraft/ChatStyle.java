package net.minecraft;

public class ChatStyle {
    private EnumChatFormatting color;
    private ClickEvent clickEvent;
    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean strikethrough;
    private boolean obfuscated;

    public ChatStyle setColor(EnumChatFormatting color) {
        this.color = color;
        return this;
    }

    public EnumChatFormatting getColor() {
        return this.color;
    }

    public ChatStyle setBold(Boolean bold) {
        this.bold = bold;
        return this;
    }

    public boolean getBold() {
        return this.bold;
    }

    public ChatStyle setItalic(Boolean italic) {
        this.italic = italic;
        return this;
    }

    public boolean getItalic() {
        return this.italic;
    }

    public ChatStyle setUnderlined(Boolean underlined) {
        this.underlined = underlined;
        return this;
    }

    public boolean getUnderlined() {
        return this.underlined;
    }

    public ChatStyle setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public boolean getStrikethrough() {
        return this.strikethrough;
    }

    public ChatStyle setObfuscated(Boolean obfuscated) {
        this.obfuscated = obfuscated;
        return this;
    }

    public ChatStyle setChatClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public ClickEvent getChatClickEvent() {
        return this.clickEvent;
    }

    public boolean getObfuscated() {
        return this.obfuscated;
    }

    public String formatString(String text) {
        StringBuilder sb = new StringBuilder();
        if (this.color != null) {
            sb.append(this.color);
        }
        if (this.obfuscated) {
            sb.append(EnumChatFormatting.OBFUSCATED);
        }
        if (this.bold) {
            sb.append(EnumChatFormatting.BOLD);
        }
        if (this.strikethrough) {
            sb.append(EnumChatFormatting.STRIKETHROUGH);
        }
        if (this.underlined) {
            sb.append(EnumChatFormatting.UNDERLINE);
        }
        if (this.italic) {
            sb.append(EnumChatFormatting.ITALIC);
        }
        return sb + text + EnumChatFormatting.RESET;
    }
}
