package com.lanche.simpleprogress;

public class LanguageManager {
    private static String currentLanguage = "en_us";

    public static String getTranslation(String key) {
        return getTranslation(key, currentLanguage);
    }

    public static String getTranslation(String key, String language) {
        // 简单的翻译表
        if ("progress.type.kill".equals(key)) {
            return language.equals("zh_cn") ? "击杀" : "Kill";
        } else if ("progress.type.obtain".equals(key)) {
            return language.equals("zh_cn") ? "获得" : "Obtain";
        } else if ("progress.type.build".equals(key)) {
            return language.equals("zh_cn") ? "建筑" : "Build";
        }
        return key;
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setCurrentLanguage(String language) {
        currentLanguage = language;
    }

    public static void initialize() {
        // 现在不在初始化时设置语言，由玩家命令控制
        // 默认使用英文
        currentLanguage = "en_us";
    }
}