package com.lanche.simpleprogress;

public class ModEvents {
    public static void register() {
        // 注册命令
        ProgressCommand.register();

        // 注册网络包
        ModNetworking.register();

        SimpleProgressMod.LOGGER.info("Simple Progress 事件已注册");
    }
}