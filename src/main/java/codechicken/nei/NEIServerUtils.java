package codechicken.nei;

import net.minecraft.ItemStack;

public class NEIServerUtils {
    public static boolean areStacksSameTypeCrafting(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null) {
            return false;
        }
        if (stack1.itemID != stack2.itemID) {
            return false;
        }
        int dmg1 = stack1.getItemSubtype();
        int dmg2 = stack2.getItemSubtype();
        return dmg1 == Short.MAX_VALUE || dmg2 == Short.MAX_VALUE || dmg1 == dmg2;
    }
}
