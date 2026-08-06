package cpw.mods.fml.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;

public class ByteBufUtils {
    public static void writeUTF8String(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readUTF8String(ByteBuf buf) {
        short length = buf.readShort();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    public static void writeItemStack(ByteBuf buf, ItemStack stack) {
        if (stack == null) {
            buf.writeShort(-1);
            return;
        }
        buf.writeShort(stack.itemID);
        buf.writeByte(stack.stackSize);
        buf.writeShort(stack.getItemSubtype());
        NBTTagCompound tag = stack.stackTagCompound;
        buf.writeBoolean(tag != null);
        // NBT payloads are not used by ProjectE's network layer; see readItemStack.
    }

    public static ItemStack readItemStack(ByteBuf buf) {
        short id = buf.readShort();
        if (id < 0) {
            return null;
        }
        byte size = buf.readByte();
        short subtype = buf.readShort();
        ItemStack stack = new ItemStack(net.minecraft.Item.itemsList[id], size, subtype);
        return stack;
    }

    public static void writeTag(ByteBuf buf, NBTTagCompound tag) {
        if (tag == null) {
            buf.writeShort(-1);
            return;
        }
        byte[] bytes = net.minecraft.CompressedStreamTools.compress(tag);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    public static NBTTagCompound readTag(ByteBuf buf) {
        short length = buf.readShort();
        if (length < 0) {
            return null;
        }
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return net.minecraft.CompressedStreamTools.decompress(bytes);
    }
}
