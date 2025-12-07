package com.lanche.simpleprogress;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientInitializer implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("SimpleProgressClient");

    @Override
    public void onInitializeClient() {
        // 不需要快捷键或GUI，只初始化客户端相关的内容
        LOGGER.info("Simple Progress 客户端初始化 - 纯命令版本");

        // 如果需要，可以在这里初始化客户端特定的网络包接收器
        ModNetworking.registerClientReceivers();
    }
}