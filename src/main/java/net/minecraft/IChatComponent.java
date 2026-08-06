package net.minecraft;

public interface IChatComponent {
    ChatStyle getChatStyle();

    String getFormattedText();

    String getUnformattedText();

    String getUnformattedTextForChat();

    IChatComponent appendSibling(IChatComponent sibling);

    IChatComponent appendText(String text);
}
