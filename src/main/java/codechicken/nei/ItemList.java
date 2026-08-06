package codechicken.nei;

import codechicken.nei.api.ItemFilter;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemList {
    public static List<ItemStack> getAllItems() {
        List<ItemStack> result = new ArrayList<>();
        for (net.minecraft.Item item : net.minecraft.Item.itemsList) {
            if (item != null) {
                result.add(new ItemStack(item));
            }
        }
        return result;
    }

    public static class EverythingItemFilter implements ItemFilter {
        @Override
        public boolean matches(ItemStack stack) {
            return true;
        }
    }

    public static class AnyMultiItemFilter implements ItemFilter {
        private final List<ItemFilter> filters;

        public AnyMultiItemFilter(List<ItemFilter> filters) {
            this.filters = filters;
        }

        @Override
        public boolean matches(ItemStack stack) {
            for (ItemFilter filter : this.filters) {
                if (filter.matches(stack)) {
                    return true;
                }
            }
            return this.filters.isEmpty();
        }
    }
}
