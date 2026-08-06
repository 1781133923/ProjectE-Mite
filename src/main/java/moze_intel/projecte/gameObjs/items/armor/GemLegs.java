package moze_intel.projecte.gameObjs.items.armor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import moze_intel.projecte.handlers.PlayerChecks;
import moze_intel.projecte.utils.EnumArmorType;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.EntityPlayer;
import net.minecraft.ServerPlayer;
import net.minecraft.ItemStack;
import net.minecraft.AxisAlignedBB;
import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.List;

public class GemLegs extends GemArmorBase
{
    public GemLegs()
    {
        super(EnumArmorType.LEGS);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltips, boolean unused, net.minecraft.Slot slot)
      {
          super.addInformation(stack, player, tooltips, unused, slot);
          tooltips.add(StatCollector.translateToLocal("pe.gem.legs.lorename"));
      }

    public void onArmorTick(World world, EntityPlayer player, ItemStack stack)
    {
        if (world.isRemote)
        {
            if (player.isSneaking() && !player.onGround && player.motionY <= 0)
            {
                player.motionY *= 2;
            }
        }

        if (player.isSneaking())
        {
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(player.posX - 3.5, player.posY - 3.5, player.posZ - 3.5, player.posX + 3.5, player.posY + 3.5, player.posZ + 3.5);
            WorldHelper.repelEntitiesInAABBFromPoint(world, box, player.posX, player.posY, player.posZ, true);
        }
    }
}
