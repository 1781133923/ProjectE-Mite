package net.minecraft;

public class ChatComponentText implements IChatComponent {
    private final String text;
    private final ChatStyle style = new ChatStyle();
    private final java.util.List<IChatComponent> siblings = new java.util.ArrayList<>();

    public ChatComponentText(String text) {
        this.text = text;
    }

    @Override
    public ChatStyle getChatStyle() {
        return this.style;
    }

    @Override
    public String getFormattedText() {
        StringBuilder sb = new StringBuilder(this.style.formatString(this.text));
        for (IChatComponent sibling : this.siblings) {
            sb.append(sibling.getFormattedText());
        }
        return sb.toString();
    }

    @Override
    public String getUnformattedText() {
        StringBuilder sb = new StringBuilder(this.text);
        for (IChatComponent sibling : this.siblings) {
            sb.append(sibling.getUnformattedText());
        }
        return sb.toString();
    }

    @Override
    public String getUnformattedTextForChat() {
        return this.getUnformattedText();
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
