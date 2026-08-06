package codechicken.nei;

import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PositionedStack {
    public List<ItemStack> items;
    public int x;
    public int y;
    private int maxSize = 0;
    private int permutation;

    public PositionedStack(Object object, int x, int y) {
        this.items = new ArrayList<>();
        if (object instanceof ItemStack) {
            this.items.add(((ItemStack) object).copy());
        } else if (object instanceof List) {
            for (Object o : (List<?>) object) {
                if (o instanceof ItemStack) {
                    this.items.add(((ItemStack) o).copy());
                }
            }
        } else if (object instanceof net.minecraft.Item) {
            this.items.add(new ItemStack((net.minecraft.Item) object));
        } else if (object instanceof net.minecraft.Block) {
            this.items.add(new ItemStack((net.minecraft.Block) object));
        }
        this.x = x;
        this.y = y;
    }

    public PositionedStack setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        for (ItemStack stack : this.items) {
            stack.stackSize = maxSize;
        }
        return this;
    }

    public PositionedStack setPermutationToRender(int index) {
        this.permutation = index % this.items.size();
        return this;
    }

    public ItemStack item() {
        return this.items.isEmpty() ? null : this.items.get(this.permutation);
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public void generatePermutations() {
    }
}
