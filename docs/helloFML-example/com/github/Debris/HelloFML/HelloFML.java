package com.github.Debris.HelloFML;

import net.fabricmc.api.ModInitializer;
import net.xiaoyu233.fml.ModResourceManager;
import net.xiaoyu233.fml.reload.event.MITEEvents;

public class HelloFML implements ModInitializer {
    public static final String MOD_ID = "hellofml";

    @Override
    public void onInitialize() {   //相当于main函数，万物起源
        ModResourceManager.addResourcePackDomain(MOD_ID);
        MITEEvents.MITE_EVENT_BUS.register(new EventListen());//注册一个事件监听类对象
    }
}