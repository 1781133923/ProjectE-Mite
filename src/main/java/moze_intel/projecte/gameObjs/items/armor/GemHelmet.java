package moze_intel.projecte.gameObjs.items.armor;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.handlers.PlayerTimers;
import moze_intel.projecte.utils.ChatHelper;
import moze_intel.projecte.utils.ClientKeyHelper;
import moze_intel.projecte.utils.EnumArmorType;
import moze_intel.projecte.utils.PEKeybind;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.Block;
import net.minecraft.Minecraft;
import net.minecraft.EntityLivingBase;
import net.minecraft.EntityLightningBolt;
import net.minecraft.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.ItemStack;
import net.minecraft.NBTTagCompound;
import net.minecraft.Potion;
import net.minecraft.PotionEffect;
import net.minecraft.ChatComponentTranslation;
import net.minecraft.EnumChatFormatting;
import net.minecraft.StatCollector;
import net.minecraft.Vec3;
import net.minecraft.World;
import thaumcraft.api.IGoggles;
import thaumcraft.api.nodes.IRevealer;

import java.util.List;

@Optional.InterfaceList(value = {@Optional.Interface(iface = "thaumcraft.api.nodes.IRevealer", modid = "Thaumcraft"), @Optional.Interface(iface = "thaumcraft.api.IGoggles", modid = "Thaumcraft")})
public class GemHelmet extends GemArmorBase implements IGoggles, IRevealer
{
    public GemHelmet()
    {
        super(EnumArmorType.HEAD);
    }

    public static boolean isNightVisionEnabled(ItemStack helm)
    {
        return helm.hasTagCompound() && helm.getTagCompound().hasKey("NightVision") && helm.getTagCompound().getBoolean("NightVision");

    }

    public static void toggleNightVision(ItemStack helm, EntityPlayer player)
    {
        if (!helm.hasTagCompound())
        {
            helm.setTagCompound(new NBTTagCompound());
        }

        boolean value;

        if (helm.stackTagCompound.hasKey("NightVision"))
        {
            helm.stackTagCompound.setBoolean("NightVision", !helm.stackTagCompound.getBoolean("NightVision"));
            value = helm.stackTagCompound.getBoolean("NightVision");
        }
        else
        {
            helm.stackTagCompound.setBoolean("NightVision", false);
            value = false;
        }

        EnumChatFormatting e = value ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
        String s = value ? "pe.gem.enabled" : "pe.gem.disabled";
        moze_intel.projecte.compat.PEChatHelper.send(player, new ChatComponentTranslation("pe.gem.nightvision_tooltip").appendText(" ")
                .appendSibling(ChatHelper.modifyColor(new ChatComponentTranslation(s), e)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltips, boolean unused, net.minecraft.Slot slot)
      {
          super.addInformation(stack, player, tooltips, unused, slot);
          tooltips.add(StatCollector.translateToLocal("pe.gem.helm.lorename"));

        tooltips.add(String.format(
                StatCollector.translateToLocal("pe.gem.nightvision.prompt"), ClientKeyHelper.getKeyName(Minecraft.getMinecraft().gameSettings.keyBindSneak), ClientKeyHelper.getKeyName(PEKeybind.ARMOR_TOGGLE)
        ));

        EnumChatFormatting e = isNightVisionEnabled(stack) ? EnumChatFormatting.GREEN : EnumChatFormatting.RED;
        String s = isNightVisionEnabled(stack) ? "pe.gem.enabled" : "pe.gem.disabled";
        tooltips.add(StatCollector.translateToLocal("pe.gem.nightvision_tooltip") + " "
                + e + StatCollector.translateToLocal(s));
    }

    public void onArmorTick(World world, EntityPlayer player, ItemStack stack)
    {
        if (world.isRemote)
        {
            int x = (int) Math.floor(player.posX);
            int y = (int) (player.posY - player.getYOffset());
            int z = (int) Math.floor(player.posZ);

            Block b = world.getBlock(x, y - 1, z);

            if ((b == Blocks.water || b == Blocks.flowing_water) && world.isAirBlock(x, y, z))
            {
                if (!player.isSneaking())
                {
                    player.motionY = 0.0d;
                    player.fallDistance = 0.0f;
                    player.onGround = true;
                }
            }
        }
		else
		{
			// Gem helmet: one heart every 20 ticks (2x the Soul stone's
			// half-heart-per-20-ticks rate).
			PlayerTimers.activateGemHeal(player);

			if (player.getHealth() < player.getMaxHealth() && PlayerTimers.canGemHeal(player))
			{
				player.heal(2.0F);
			}

            if (isNightVisionEnabled(stack))
            {
                player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 220, 0));
            }
            else
            {
                player.removePotionEffect(Potion.nightVision.id);
            }

            if (player.isInWater())
            {
                player.setAir(300);
            }
        }
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean showIngamePopups(ItemStack stack, EntityLivingBase player)
    {
        return true;
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean showNodes(ItemStack stack, EntityLivingBase player)
    {
        return true;
    }

    public void doZap(EntityPlayer player)
    {
        if (ProjectEConfig.offensiveAbilities)
        {
            Vec3 strikePos = PlayerHelper.getBlockLookingAt(player, 120.0F);
            if (strikePos != null)
			{
				player.worldObj.addWeatherEffect(new EntityLightningBolt(player.worldObj, strikePos.xCoord, strikePos.yCoord, strikePos.zCoord));
			}
        }
    }
}
