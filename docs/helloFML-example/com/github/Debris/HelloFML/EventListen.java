package com.github.Debris.HelloFML;

import com.github.Debris.HelloFML.blocks.Blocks;
import com.github.Debris.HelloFML.items.Items;
import com.google.common.eventbus.Subscribe;
import net.xiaoyu233.fml.reload.event.*;
import net.xiaoyu233.fml.reload.utils.IdUtil;

import java.util.Objects;

public class EventListen {
    //物品注册
    @Subscribe
    public void onItemRegister(ItemRegistryEvent event) {
        Items.registerItems(event);
        Blocks.registerItemBlocks(event);
    }

    //合成方式注册
    @Subscribe
    public void onRecipeRegister(RecipeRegistryEvent event) {
        Items.registerRecipes(event);
        Blocks.registerRecipes(event);
    }

    //玩家登录事件
    @Subscribe
    public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
    }

    //指令事件
    @Subscribe
    public void handleChatCommand(HandleChatCommandEvent event) {
        if(Objects.equals(event.getCommand(), "Hello")){
            event.getPlayer().addChatMessage("Hello,FML!");
        }
    }

    //实体注册
    @Subscribe
    public void onEntityRegister(EntityRegisterEvent event) {
    }

    //实体渲染注册
    @Subscribe
    public void onEntityRendererRegistry(EntityRendererRegistryEvent event) {
    }

    //方块实体注册
    @Subscribe
    public void onTileEntityRegister(TileEntityRegisterEvent event) {
    }

    //方块实体渲染注册
    @Subscribe
    public void onTileEntityRendererRegister(TileEntityRendererRegisterEvent event) {
    }

    //声音注册
    @Subscribe
    public void onSoundsRegister(SoundsRegisterEvent event) {
    }

    public static int getNextEntityID() {
        return IdUtil.getNextEntityID();
    }
}
