package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableList;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.ItemStack;
import net.minecraft.Achievement;
import net.minecraftforge.common.AchievementPage;


public final class AchievementHandler
{

	public final static Achievement PHIL_STONE = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "phil_stone", 0, 2, ObjHandler.philosStone, null).initIndependentStat().registerStat());
	public final static Achievement ALCH_CHEST = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "alch_chest", 0, -2, ObjHandler.alchChest, null).initIndependentStat().registerStat());
	public final static Achievement ALCH_BAG = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "alch_bag", 0, -4, ObjHandler.alchBag, ALCH_CHEST).registerStat());
	public final static Achievement TRANSMUTATION = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "transmutation", 0, 0, ObjHandler.transmuteStone, PHIL_STONE).registerStat());
	public final static Achievement CONDENSER = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "condenser", -2, -2, ObjHandler.condenser, ALCH_CHEST).setSpecial().registerStat());
	public final static Achievement COLLECTOR = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "collector", -2, -4, ObjHandler.energyCollector, CONDENSER).setSpecial().registerStat());
	public final static Achievement RELAY = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "relay", -4, -4, ObjHandler.relay, COLLECTOR).setSpecial().registerStat());
	public final static Achievement PORTABLE_TRANSMUTATION = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "portable_transmutation", -2, 0, ObjHandler.transmutationTablet, TRANSMUTATION).setSpecial().registerStat());
	public final static Achievement DARK_MATTER = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "dark_matter", 2, 0, new ItemStack(ObjHandler.matter, 1, 0), null).initIndependentStat().registerStat());
	public final static Achievement RED_MATTER = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "red_matter", 2, -2, new ItemStack(ObjHandler.matter, 1, 1), DARK_MATTER).setSpecial().registerStat());
	public final static Achievement DM_BLOCK = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "dm_block", 4, 0, new ItemStack(ObjHandler.matterBlock, 1, 0), DARK_MATTER).setSpecial().registerStat());
	public final static Achievement RM_BLOCK = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "rm_block", 4, -2, new ItemStack(ObjHandler.matterBlock, 1, 1), RED_MATTER).setSpecial().registerStat());
	public final static Achievement DM_FURNACE = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "dm_furnace", 6, 0, ObjHandler.dmFurnaceOff, DM_BLOCK).setSpecial().registerStat());
	public final static Achievement RM_FURNACE = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "rm_furnace", 6, -2, ObjHandler.rmFurnaceOff, RM_BLOCK).setSpecial().registerStat());
	public final static Achievement DM_PICK = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "dm_pick", 2, 2, ObjHandler.dmPick, DARK_MATTER).registerStat());
	public final static Achievement RM_PICK = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "rm_pick", 2, 4, ObjHandler.rmPick, DM_PICK).setSpecial().registerStat());
	public final static Achievement KLEIN_BASIC = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "klein", 0, 4, new ItemStack(ObjHandler.kleinStars, 1, 0), PHIL_STONE).registerStat());
	public final static Achievement KLEIN_MASTER = (Achievement) (new Achievement(net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID(), "klein_big", -2, 4, new ItemStack(ObjHandler.kleinStars, 1, 5), KLEIN_BASIC).setSpecial().registerStat());

	public static ImmutableList<Achievement> list = ImmutableList.of(
			PHIL_STONE, ALCH_CHEST, ALCH_BAG, TRANSMUTATION, CONDENSER,
			COLLECTOR, RELAY, PORTABLE_TRANSMUTATION, DARK_MATTER, RED_MATTER, DM_BLOCK,
			RM_BLOCK, DM_FURNACE, RM_FURNACE, DM_PICK, RM_PICK, KLEIN_BASIC, KLEIN_MASTER
	);

	public static void init()
	{
		AchievementPage.registerAchievementPage(new AchievementPage("ProjectE", list.toArray(new Achievement[list.size()])));
	}

	public static Achievement getAchievementForItem(ItemStack stack)
	{
		if (stack == null)
		{
			return null;
		}
		
		for (Achievement ach : list)
		{
			ItemStack s = ach.theItemStack;
			
			if (s.getItem() == stack.getItem() && s.getItemDamage() == stack.getItemDamage())
			{
				return ach;
			}
		}
		
		return null;
	}
}
