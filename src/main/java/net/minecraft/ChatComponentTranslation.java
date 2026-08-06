package net.minecraft;

public class ChatComponentTranslation implements IChatComponent {
    private final String key;
    private final Object[] args;
    private final ChatStyle style = new ChatStyle();
    private final java.util.List<IChatComponent> siblings = new java.util.ArrayList<>();

    public ChatComponentTranslation(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    @Override
    public ChatStyle getChatStyle() {
        return this.style;
    }

    @Override
    public String getFormattedText() {
        StringBuilder sb = new StringBuilder(this.style.formatString(this.getUnformattedTextForChat()));
        for (IChatComponent sibling : this.siblings) {
            sb.append(sibling.getFormattedText());
        }
        return sb.toString();
    }

    @Override
    public String getUnformattedText() {
        return this.getUnformattedTextForChat();
    }

    @Override
    public String getUnformattedTextForChat() {
        String translated = StatCollector.translateToLocal(this.key);
        if (translated.equals(this.key)) {
            translated = StatCollector.translateToLocal("en_US." + this.key);
        }
        try {
            return String.format(translated, this.args);
        } catch (Exception e) {
            return translated;
        }
    }

    @Override
    public IChatComponent appendSibling(IChatComponent sibling) {
        this.siblings.add(sibling);
        return this;
    }

    @Override
    public IChatComponent appendText(String text) {
        this.siblings.add(new ChatComponentText(text));
        return this;
    }
}
