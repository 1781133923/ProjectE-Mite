package net.minecraftforge.common;

import net.minecraft.Entity;
import net.minecraft.NBTTagCompound;
import net.minecraft.World;

public interface IExtendedEntityProperties {
    void saveNBTData(NBTTagCompound compound);

    void loadNBTData(NBTTagCompound compound);

    void init(Entity entity, World world);
}
