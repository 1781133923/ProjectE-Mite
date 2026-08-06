package moze_intel.projecte.gameObjs.items.armor;

import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.items.IFlightProvider;
import moze_intel.projecte.gameObjs.items.IStepAssister;
import moze_intel.projecte.utils.ChatHelper;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.EnumArmorType;
import moze_intel.projecte.utils.PEKeybind;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.AttributeModifier;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.ChatComponentTranslation;
import net.minecraft.EnumChatFormatting;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.List;

public class GemFeet extends GemArmorBase implements IFlightProvider, IStepAssister
{
    public GemFeet()
    {
        super(EnumArmorType.FEET);
    }

    public void toggleStepAssist(ItemStack boots, EntityPlayer player)
    {
        if (!boots.hasTagCompound())
        {
            boots.setTagCompound(new NBTTagCompound());
        }

        boolean value;

        if (boots.stackTagCompound.hasKey("StepAssist"))
        {
            boots.stackTagCompound.setBoolean("StepAssist", !boots.stackTagCompound.getBoolean("StepAssist"));
            value = boots.stackTagCompound.getBoolean("StepAssist");
        }
        else
        {
            boots.stackTagCompound.setBoolean("StepAssist", false);
            value = false;
        }

        EnumChatFormatting e = value ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
        String s = value ? "pe.gem.enabled" : "pe.gem.disabled";
        moze_intel.projecte.compat.PEChatHelper.send(player, new ChatComponentTranslation("pe.gem.stepassist_tooltip").appendText(" ")
                .appendSibling(ChatHelper.modifyColor(new ChatComponentTranslation(s), e)));
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
        tooltips.add(String.format(
                StatCollector.translateToLocal("pe.gem.stepassist.prompt"), ClientKeyHelper.getKeyName(PEKeybind.ARMOR_TOGGLE)));

        EnumChatFormatting e = canStep(stack) ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
        String s = canStep(stack) ? "pe.gem.enabled" : "pe.gem.disabled";
        tooltips.add(StatCollector.translateToLocal("pe.gem.stepassist_tooltip") + " "
                + e + StatCollector.translateToLocal(s));
    }

    private boolean canStep(ItemStack stack)
    {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey("StepAssist") && stack.getTagCompound().getBoolean("StepAssist");
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

    @Override
    public boolean canAssistStep(ItemStack stack, ServerPlayer player)
    {
        return player.getCurrentArmor(0) == stack
                && canStep(stack);
    }
}
