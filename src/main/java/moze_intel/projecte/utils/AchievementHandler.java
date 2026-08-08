package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableList;
import moze_intel.projecte.gameObjs.ObjHandler;
import net.minecraft.ItemStack;
import net.minecraft.Achievement;
import net.minecraftforge.common.AchievementPage;


public final class AchievementHandler
{

	public final static Achievement PHIL_STONE = (Achievement) (new Achievement(nextAchievementId(), "phil_stone", 0, 2, ObjHandler.philosStone, null).initIndependentStat().registerStat());
	public final static Achievement ALCH_CHEST = (Achievement) (new Achievement(nextAchievementId(), "alch_chest", 0, -2, ObjHandler.alchChest, null).initIndependentStat().registerStat());
	public final static Achievement ALCH_BAG = (Achievement) (new Achievement(nextAchievementId(), "alch_bag", 0, -4, ObjHandler.alchBag, ALCH_CHEST).registerStat());
	public final static Achievement TRANSMUTATION = (Achievement) (new Achievement(nextAchievementId(), "transmutation", 0, 0, ObjHandler.transmuteStone, PHIL_STONE).registerStat());
	public final static Achievement CONDENSER = (Achievement) (new Achievement(nextAchievementId(), "condenser", -2, -2, ObjHandler.condenser, ALCH_CHEST).setSpecial().registerStat());
	public final static Achievement COLLECTOR = (Achievement) (new Achievement(nextAchievementId(), "collector", -2, -4, ObjHandler.energyCollector, CONDENSER).setSpecial().registerStat());
	public final static Achievement RELAY = (Achievement) (new Achievement(nextAchievementId(), "relay", -4, -4, ObjHandler.relay, COLLECTOR).setSpecial().registerStat());
	public final static Achievement PORTABLE_TRANSMUTATION = (Achievement) (new Achievement(nextAchievementId(), "portable_transmutation", -2, 0, ObjHandler.transmutationTablet, TRANSMUTATION).setSpecial().registerStat());
	public final static Achievement DARK_MATTER = (Achievement) (new Achievement(nextAchievementId(), "dark_matter", 2, 0, new ItemStack(ObjHandler.matter, 1, 0), null).initIndependentStat().registerStat());
	public final static Achievement RED_MATTER = (Achievement) (new Achievement(nextAchievementId(), "red_matter", 2, -2, new ItemStack(ObjHandler.matter, 1, 1), DARK_MATTER).setSpecial().registerStat());
	public final static Achievement DM_BLOCK = (Achievement) (new Achievement(nextAchievementId(), "dm_block", 4, 0, new ItemStack(ObjHandler.matterBlock, 1, 0), DARK_MATTER).setSpecial().registerStat());
	public final static Achievement RM_BLOCK = (Achievement) (new Achievement(nextAchievementId(), "rm_block", 4, -2, new ItemStack(ObjHandler.matterBlock, 1, 1), RED_MATTER).setSpecial().registerStat());
	public final static Achievement DM_FURNACE = (Achievement) (new Achievement(nextAchievementId(), "dm_furnace", 6, 0, ObjHandler.dmFurnaceOff, DM_BLOCK).setSpecial().registerStat());
	public final static Achievement RM_FURNACE = (Achievement) (new Achievement(nextAchievementId(), "rm_furnace", 6, -2, ObjHandler.rmFurnaceOff, RM_BLOCK).setSpecial().registerStat());
	public final static Achievement DM_PICK = (Achievement) (new Achievement(nextAchievementId(), "dm_pick", 2, 2, ObjHandler.dmPick, DARK_MATTER).registerStat());
	public final static Achievement RM_PICK = (Achievement) (new Achievement(nextAchievementId(), "rm_pick", 2, 4, ObjHandler.rmPick, DM_PICK).setSpecial().registerStat());
	public final static Achievement KLEIN_BASIC = (Achievement) (new Achievement(nextAchievementId(), "klein", 0, 4, new ItemStack(ObjHandler.kleinStars, 1, 0), PHIL_STONE).registerStat());
	public final static Achievement KLEIN_MASTER = (Achievement) (new Achievement(nextAchievementId(), "klein_big", -2, 4, new ItemStack(ObjHandler.kleinStars, 1, 5), KLEIN_BASIC).setSpecial().registerStat());

	public static ImmutableList<Achievement> list = ImmutableList.of(
			PHIL_STONE, ALCH_CHEST, ALCH_BAG, TRANSMUTATION, CONDENSER,
			COLLECTOR, RELAY, PORTABLE_TRANSMUTATION, DARK_MATTER, RED_MATTER, DM_BLOCK,
			RM_BLOCK, DM_FURNACE, RM_FURNACE, DM_PICK, RM_PICK, KLEIN_BASIC, KLEIN_MASTER
	);	/**
	 * Takes the next achievement id from the loader's shared counter but skips
	 * any id that is already taken (other mods, e.g. Extreme, register their
	 * achievements from the same counter while our ObjHandler static init
	 * runs, so the plain counter can collide with an existing stat).
	 */
	private static int nextAchievementId()
	{
		int id;
		do
		{
			id = net.xiaoyu233.fml.reload.utils.IdUtil.getNextAchievementID();
		}
		while (statIdTaken(id));
		return id;
	}

	/**
	 * StatList.oneShotStats is protected; scan the public allStats list for an
	 * already-registered stat with the same id.
	 */
	private static boolean statIdTaken(int id)
	{
		for (Object o : net.minecraft.StatList.allStats)
		{
			if (o instanceof net.minecraft.StatBase && ((net.minecraft.StatBase) o).statId == id)
			{
				return true;
			}
		}
		return false;
	}

	public static void init()
	{
		AchievementPage.registerAchievementPage(new AchievementPage("ProjectE", list.toArray(new Achievement[list.size()])));
	}

	public static boolean isProjectEAchievement(Achievement achievement)
	{
		return list.contains(achievement);
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
