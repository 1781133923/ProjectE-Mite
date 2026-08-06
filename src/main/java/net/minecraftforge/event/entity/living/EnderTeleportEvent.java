package net.minecraftforge.event.entity.living;

import net.minecraft.EntityLivingBase;

public class EnderTeleportEvent extends LivingEvent {
    public double targetX;
    public double targetY;
    public double targetZ;
    public final double targetXStart;
    public final double targetYStart;
    public final double targetZStart;
    public float attackDamage;

    public EnderTeleportEvent(EntityLivingBase entity, double targetX, double targetY, double targetZ, float attackDamage) {
        super(entity);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.targetXStart = targetX;
        this.targetYStart = targetY;
        this.targetZStart = targetZ;
        this.attackDamage = attackDamage;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
