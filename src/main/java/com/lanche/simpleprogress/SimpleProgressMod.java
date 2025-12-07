package com.lanche.simpleprogress;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProgressMod implements ModInitializer {
    public static final String MOD_ID = "simpleprogress";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Simple Progress v1.0.3 initializing");

        // 初始化配置
        ModConfig.init();

        // 初始化语言管理器（虽然大部分文本硬编码，但仍支持类型翻译）
        LanguageManager.initialize();

        // 注册事件和命令
        ModEvents.register();

        LOGGER.info("Simple Progress initialized successfully!");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}