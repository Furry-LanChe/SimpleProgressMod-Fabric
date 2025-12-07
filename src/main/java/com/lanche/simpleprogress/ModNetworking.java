package com.lanche.simpleprogress;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class ModNetworking {
    public static final Identifier SYNC_PROGRESS_PACKET = SimpleProgressMod.id("sync_progress");

    public static void register() {
        SimpleProgressMod.LOGGER.info("注册网络包通道...");
    }

    public static void registerClientReceivers() {
        // 如果需要客户端接收进度同步，可以在这里注册
        // 目前纯命令版本可能不需要
    }
}