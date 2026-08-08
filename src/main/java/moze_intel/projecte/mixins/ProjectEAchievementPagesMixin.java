package moze_intel.projecte.mixins;

import net.minecraft.Achievement;
import net.minecraft.AchievementList;
import net.minecraft.GuiAchievements;
import net.minecraft.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the achievements screen into two pages - the vanilla achievements
 * and the ProjectE achievements - selected with two buttons at the top of
 * the screen. Only the achievements of the current page are rendered.
 */
@Mixin(GuiAchievements.class)
public abstract class ProjectEAchievementPagesMixin
{
    @Unique
    private static int projecte$currentPage = 0;

    @Unique
    private static List<Achievement> projecte$filteredCache;

    @Unique
    private static int projecte$filteredForPage = -1;

    @Unique
    private static List<Achievement> projecte$pageAchievements()
    {
        if (projecte$filteredForPage != projecte$currentPage)
        {
            List<Achievement> result = new ArrayList<Achievement>();
            for (Object o : AchievementList.achievementList)
            {
                Achievement achievement = (Achievement) o;
                boolean isProjectE = moze_intel.projecte.utils.AchievementHandler.isProjectEAchievement(achievement);
                if (projecte$currentPage == 1 ? isProjectE : !isProjectE)
                {
                    result.add(achievement);
                }
            }
            projecte$filteredCache = result;
            projecte$filteredForPage = projecte$currentPage;
        }
        return projecte$filteredCache;
    }

    @Redirect(method = "genAchievementBackground", at = @At(value = "FIELD", target = "Lnet/minecraft/AchievementList;achievementList:Ljava/util/List;"))
    private List<Achievement> projecte$filterAchievementList()
    {
        return projecte$pageAchievements();
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    private void projecte$addPageButtons(CallbackInfo ci)
    {
        net.minecraft.GuiScreen self = (net.minecraft.GuiScreen) (Object) this;
        try
        {
            // buttonList is declared in GuiScreen (a superclass), which Mixin
            // @Shadow cannot resolve on the target class, so reach it through
            // reflection. width is public, so it can be read directly.
            java.lang.reflect.Field field = net.minecraft.GuiScreen.class.getDeclaredField("buttonList");
            field.setAccessible(true);
            List buttons = (List) field.get(self);
            int midX = self.width / 2;
            buttons.add(new GuiButton(100, midX - 100, 26, 96, 20, "\u539f\u7248\u6210\u5c31"));
            buttons.add(new GuiButton(101, midX + 4, 26, 96, 20, "ProjectE"));
        }
        catch (Throwable t)
        {
            // Never break the achievements screen because of the page buttons.
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void projecte$handlePageButton(GuiButton button, CallbackInfo ci)
    {
        if (button.id == 100)
        {
            projecte$currentPage = 0;
        }
        else if (button.id == 101)
        {
            projecte$currentPage = 1;
        }
    }
}