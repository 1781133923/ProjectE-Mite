package net.minecraftforge.common;

import net.minecraft.Achievement;

import java.util.ArrayList;
import java.util.List;

public class AchievementPage {
    private static final List<AchievementPage> PAGES = new ArrayList<>();

    private final String name;
    private final List<Achievement> achievements = new ArrayList<>();

    public AchievementPage(String name, Achievement... achievements) {
        this.name = name;
        for (Achievement achievement : achievements) {
            this.achievements.add(achievement);
        }
    }

    public static void registerAchievementPage(AchievementPage page) {
        PAGES.add(page);
    }

    public static List<AchievementPage> getAchievementPages() {
        return PAGES;
    }

    public String getName() {
        return this.name;
    }

    public List<Achievement> getAchievements() {
        return this.achievements;
    }
}
