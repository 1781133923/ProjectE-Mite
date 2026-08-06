package cpw.mods.fml.common.event;

import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public class FMLInterModComms {
    public static boolean sendMessage(String modId, String key, Object value) {
        return true;
    }

    public static class IMCEvent {
        private final List<IMCMessage> messages = new ArrayList<>();

        public List<IMCMessage> getMessages() {
            return this.messages;
        }
    }

    public static class IMCMessage {
        public final String key;
        public final String sender;
        private final String stringValue;
        private final ItemStack itemStackValue;
        private final NBTTagCompound nbtValue;

        public IMCMessage(String key, String sender, String stringValue) {
            this(key, sender, stringValue, null, null);
        }

        public IMCMessage(String key, String sender, ItemStack itemStackValue) {
            this(key, sender, null, itemStackValue, null);
        }

        public IMCMessage(String key, String sender, NBTTagCompound nbtValue) {
            this(key, sender, null, null, nbtValue);
        }

        private IMCMessage(String key, String sender, String stringValue, ItemStack itemStackValue, NBTTagCompound nbtValue) {
            this.key = key;
            this.sender = sender;
            this.stringValue = stringValue;
            this.itemStackValue = itemStackValue;
            this.nbtValue = nbtValue;
        }

        public String getStringValue() {
            return this.stringValue;
        }

        public String getSender() {
            return this.sender;
        }

        public ItemStack getItemStackValue() {
            return this.itemStackValue;
        }

        public NBTTagCompound getNBTValue() {
            return this.nbtValue;
        }

        public boolean isStringMessage() {
            return this.stringValue != null;
        }

        public boolean isItemStackMessage() {
            return this.itemStackValue != null;
        }

        public boolean isNBTMessage() {
            return this.nbtValue != null;
        }
    }
}
