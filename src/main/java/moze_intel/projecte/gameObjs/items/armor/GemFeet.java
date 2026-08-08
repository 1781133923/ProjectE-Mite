package moze_intel.projecte.gameObjs.items.armor;

import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.utils.EnumArmorType;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.AttributeModifier;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.ItemStack;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.List;

public class GemFeet extends GemArmorBase implements IFlightProvider
{
    public GemFeet()
    {
        super(EnumArmorType.FEET);
    }

    public void onArmorTick(World world, EntityPlayer player, ItemStack stack)
    {
        if (!world.isRemote)
        {
            ServerPlayer playerMP = ((ServerPlayer) player);
            playerMP.fallDistance = 0;
        }
        else
        {
            if (!player.capabilities.isFlying && PECore.proxy.isJumpPressed())
            {
                player.motionY += 0.1;
            }

            if (!player.onGround)
            {
                if (player.motionY <= 0)
                {
                    player.motionY *= 0.90;
                }
                if (!player.capabilities.isFlying)
                {
                    if (player.moveForward < 0)
                    {
                        player.motionX *= 0.9;
                        player.motionZ *= 0.9;
                    } else if (player.moveForward > 0 && player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ < 3)
                    {
                        player.motionX *= 1.1;
                        player.motionZ *= 1.1;
                    }
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltips, boolean unused, net.minecraft.Slot slot)
      {
          super.addInformation(stack, player, tooltips, unused, slot);
          tooltips.add(StatCollector.translateToLocal("pe.gem.feet.lorename"));
    }

    public Multimap getAttributeModifiers(ItemStack stack)
    {
        Multimap multimap = com.google.common.collect.HashMultimap.create();
        multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(),
                new AttributeModifier(java.util.UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), "Armor modifier", 1.0, 2));
        return multimap;
    }

    @Override
    public boolean canProvideFlight(ItemStack stack, ServerPlayer player)
    {
        return player.getCurrentArmor(0) == stack;
    }
}