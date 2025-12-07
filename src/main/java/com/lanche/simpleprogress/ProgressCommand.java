package com.lanche.simpleprogress;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProgressCommand {

    // 存储玩家语言偏好
    private static final Map<UUID, String> playerLanguages = new HashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher, registryAccess);
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess) {
        // 主命令 /progress
        dispatcher.register(CommandManager.literal("progress")
                .executes(context -> {
                    sendHelpMessage(context);
                    return 1;
                })
                .then(CommandManager.literal("help")
                        .executes(context -> {
                            sendHelpMessage(context);
                            return 1;
                        })
                )
                .then(CommandManager.literal("add")
                        .requires(source -> source.isExecutedByPlayer())
                        .then(CommandManager.argument("title", StringArgumentType.greedyString())
                                .executes(context -> addProgress(
                                        context,
                                        StringArgumentType.getString(context, "title"),
                                        "minecraft:zombie",
                                        10,
                                        ProgressManager.ProgressType.KILL
                                ))
                                .then(CommandManager.argument("target", StringArgumentType.string())
                                        .executes(context -> addProgress(
                                                context,
                                                StringArgumentType.getString(context, "title"),
                                                StringArgumentType.getString(context, "target"),
                                                10,
                                                ProgressManager.ProgressType.KILL
                                        ))
                                        .then(CommandManager.argument("count", IntegerArgumentType.integer(1))
                                                .executes(context -> addProgress(
                                                        context,
                                                        StringArgumentType.getString(context, "title"),
                                                        StringArgumentType.getString(context, "target"),
                                                        IntegerArgumentType.getInteger(context, "count"),
                                                        ProgressManager.ProgressType.KILL
                                                ))
                                                .then(CommandManager.literal("kill")
                                                        .executes(context -> addProgress(
                                                                context,
                                                                StringArgumentType.getString(context, "title"),
                                                                StringArgumentType.getString(context, "target"),
                                                                IntegerArgumentType.getInteger(context, "count"),
                                                                ProgressManager.ProgressType.KILL
                                                        ))
                                                )
                                                .then(CommandManager.literal("obtain")
                                                        .executes(context -> addProgress(
                                                                context,
                                                                StringArgumentType.getString(context, "title"),
                                                                StringArgumentType.getString(context, "target"),
                                                                IntegerArgumentType.getInteger(context, "count"),
                                                                ProgressManager.ProgressType.OBTAIN
                                                        ))
                                                )
                                                .then(CommandManager.literal("build")
                                                        .executes(context -> addProgress(
                                                                context,
                                                                StringArgumentType.getString(context, "title"),
                                                                StringArgumentType.getString(context, "target"),
                                                                IntegerArgumentType.getInteger(context, "count"),
                                                                ProgressManager.ProgressType.BUILD
                                                        ))
                                                )
                                        )
                                )
                        )
                )
                .then(CommandManager.literal("list")
                        .requires(source -> source.isExecutedByPlayer())
                        .executes(context -> listAllProgresses(context))
                        .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> listProgressesByPage(
                                        context,
                                        IntegerArgumentType.getInteger(context, "page")
                                ))
                        )
                )
                .then(CommandManager.literal("view")
                        .requires(source -> source.isExecutedByPlayer())
                        .then(CommandManager.argument("id", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player != null) {
                                        List<ProgressManager.CustomProgress> progresses = ProgressManager.getPlayerData(player);
                                        for (ProgressManager.CustomProgress progress : progresses) {
                                            builder.suggest(progress.id);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> viewProgress(
                                        context,
                                        StringArgumentType.getString(context, "id")
                                ))
                        )
                )
                .then(CommandManager.literal("update")
                        .requires(source -> source.isExecutedByPlayer())
                        .then(CommandManager.argument("id", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player != null) {
                                        List<ProgressManager.CustomProgress> progresses = ProgressManager.getPlayerData(player);
                                        for (ProgressManager.CustomProgress progress : progresses) {
                                            builder.suggest(progress.id);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(CommandManager.argument("current", IntegerArgumentType.integer(0))
                                        .executes(context -> updateProgress(
                                                context,
                                                StringArgumentType.getString(context, "id"),
                                                IntegerArgumentType.getInteger(context, "current")
                                        ))
                                )
                        )
                )
                .then(CommandManager.literal("delete")
                        .requires(source -> source.isExecutedByPlayer())
                        .then(CommandManager.argument("id", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player != null) {
                                        List<ProgressManager.CustomProgress> progresses = ProgressManager.getPlayerData(player);
                                        for (ProgressManager.CustomProgress progress : progresses) {
                                            builder.suggest(progress.id);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> deleteProgress(
                                        context,
                                        StringArgumentType.getString(context, "id")
                                ))
                        )
                )
                .then(CommandManager.literal("clear")
                        .requires(source -> source.isExecutedByPlayer())
                        .executes(context -> clearProgresses(context))
                        .then(CommandManager.literal("confirm")
                                .executes(context -> confirmClearProgresses(context))
                        )
                )
                .then(CommandManager.literal("stats")
                        .requires(source -> source.isExecutedByPlayer())
                        .executes(context -> showStats(context))
                )
                .then(CommandManager.literal("lang")
                        .requires(source -> source.isExecutedByPlayer())
                        .executes(context -> showCurrentLanguage(context))
                        .then(CommandManager.literal("en_us")
                                .executes(context -> setLanguage(context, "en_us"))
                        )
                        .then(CommandManager.literal("zh_cn")
                                .executes(context -> setLanguage(context, "zh_cn"))
                        )
                        .then(CommandManager.literal("reset")
                                .executes(context -> resetLanguage(context))
                        )
                )
        );

        // 快捷命令 /prog
        dispatcher.register(CommandManager.literal("prog")
                .executes(context -> {
                    sendHelpMessage(context);
                    return 1;
                })
                .then(CommandManager.literal("list")
                        .requires(source -> source.isExecutedByPlayer())
                        .executes(context -> listAllProgresses(context))
                )
        );

        SimpleProgressMod.LOGGER.info("Simple Progress 命令已注册: /progress, /prog");
    }

    private static int addProgress(CommandContext<ServerCommandSource> context,
                                   String title, String target, int count,
                                   ProgressManager.ProgressType type) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            try {
                ProgressManager.CustomProgress progress = new ProgressManager.CustomProgress();
                progress.title = title;
                progress.type = type;
                progress.target = target;
                progress.targetCount = count;
                progress.current = 0;
                progress.completed = false;

                ProgressManager.addProgress(player, progress);

                String typeDisplayName = getTypeDisplayName(type, player.getUuid());
                String message = getPlayerLanguage(player.getUuid()).equals("zh_cn") ?
                        "§a✓ §7进度已添加: §f" + title + "\n§7ID: §e" + progress.id.substring(0, 8) + "..." +
                                "\n§7类型: " + type.getColorCode() + typeDisplayName + "\n§7目标: §a" + target + " §7x§e" + count +
                                "\n§7进度: §e0§7/§a" + count + "\n§7使用 §e/progress view " + progress.id + " §7查看详情" :
                        "§a✓ §7Progress added: §f" + title + "\n§7ID: §e" + progress.id.substring(0, 8) + "..." +
                                "\n§7Type: " + type.getColorCode() + typeDisplayName + "\n§7Target: §a" + target + " §7x§e" + count +
                                "\n§7Progress: §e0§7/§a" + count + "\n§7Use §e/progress view " + progress.id + " §7to view details";

                source.sendMessage(Text.literal(message));
                return 1;
            } catch (Exception e) {
                String errorMsg = getPlayerLanguage(source.getPlayer().getUuid()).equals("zh_cn") ?
                        "§c✗ §7添加进度失败: " + e.getMessage() :
                        "§c✗ §7Failed to add progress: " + e.getMessage();
                source.sendMessage(Text.literal(errorMsg));
                return 0;
            }
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int listAllProgresses(CommandContext<ServerCommandSource> context) {
        return listProgressesByPage(context, 1);
    }

    private static int listProgressesByPage(CommandContext<ServerCommandSource> context, int page) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            List<ProgressManager.CustomProgress> progresses = ProgressManager.getPlayerData(player);

            String lang = getPlayerLanguage(player.getUuid());
            boolean isChinese = lang.equals("zh_cn");

            if (progresses.isEmpty()) {
                String message = isChinese ?
                        "§a[SimpleProgress] §7你还没有任何进度记录\n§7使用 §e/progress add <标题> §7添加进度" :
                        "§a[SimpleProgress] §7You don't have any progress records\n§7Use §e/progress add <title> §7to add progress";
                source.sendMessage(Text.literal(message));
                return 1;
            }

            int pageSize = 8;
            int totalPages = (progresses.size() + pageSize - 1) / pageSize;
            page = Math.min(Math.max(1, page), totalPages);

            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, progresses.size());

            // 顶部信息
            String header = isChinese ?
                    "§6=== 进度列表 (§e" + progresses.size() + "§6) 第§e" + page + "§6/§a" + totalPages + "§6页 ===" :
                    "§6=== Progress List (§e" + progresses.size() + "§6) Page §e" + page + "§6/§a" + totalPages + "§6 ===";

            String info = isChinese ?
                    "§7使用 §e/progress view <ID> §7查看详细信息" :
                    "§7Use §e/progress view <ID> §7to view details";

            source.sendMessage(Text.literal(header));
            source.sendMessage(Text.literal(info));

            // 列表项
            for (int i = startIndex; i < endIndex; i++) {
                var progress = progresses.get(i);
                String statusIcon = progress.completed ? "§a✓" : "§e⏳";
                String progressBar = createProgressBar(progress.current, progress.targetCount, 20);
                String percentage = String.format("%.1f%%", progress.getProgress() * 100);
                String typeDisplayName = getTypeDisplayName(progress.type, player.getUuid());

                Text message = isChinese ?
                        Text.literal(statusIcon + " §7" + (i + 1) + ". §f" + progress.title)
                                .append(Text.literal(" §7[" + progress.type.getColorCode() + typeDisplayName + "§7]"))
                                .append(Text.literal("\n   §7进度: " + progressBar + " §e" + percentage))
                                .append(Text.literal("\n   §7ID: §e" + progress.id.substring(0, 8) + "..."))
                                .append(Text.literal("\n   §7目标: §a" + progress.target + " §7x§e" + progress.targetCount))
                                .append(Text.literal("\n   §7完成: §e" + progress.current + "§7/§a" + progress.targetCount))
                                .append(Text.literal("\n   §7操作: ")
                                        .append(Text.literal("§a[查看]")
                                                .styled(style -> style.withClickEvent(
                                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress view " + progress.id)
                                                ).withHoverEvent(
                                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Text.literal(isChinese ? "点击查看详情" : "Click to view details"))
                                                )))
                                        .append(Text.literal(" §c[删除]")
                                                .styled(style -> style.withClickEvent(
                                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress delete " + progress.id)
                                                ).withHoverEvent(
                                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Text.literal(isChinese ? "点击删除进度" : "Click to delete progress"))
                                                )))
                                ) :
                        Text.literal(statusIcon + " §7" + (i + 1) + ". §f" + progress.title)
                                .append(Text.literal(" §7[" + progress.type.getColorCode() + typeDisplayName + "§7]"))
                                .append(Text.literal("\n   §7Progress: " + progressBar + " §e" + percentage))
                                .append(Text.literal("\n   §7ID: §e" + progress.id.substring(0, 8) + "..."))
                                .append(Text.literal("\n   §7Target: §a" + progress.target + " §7x§e" + progress.targetCount))
                                .append(Text.literal("\n   §7Completed: §e" + progress.current + "§7/§a" + progress.targetCount))
                                .append(Text.literal("\n   §7Actions: ")
                                        .append(Text.literal("§a[View]")
                                                .styled(style -> style.withClickEvent(
                                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress view " + progress.id)
                                                ).withHoverEvent(
                                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Text.literal(isChinese ? "点击查看详情" : "Click to view details"))
                                                )))
                                        .append(Text.literal(" §c[Delete]")
                                                .styled(style -> style.withClickEvent(
                                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress delete " + progress.id)
                                                ).withHoverEvent(
                                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                Text.literal(isChinese ? "点击删除进度" : "Click to delete progress"))
                                                )))
                                );

                source.sendMessage(message);
            }

            // 分页导航
            if (totalPages > 1) {
                Text navigation = Text.literal(isChinese ? "§7页面: " : "§7Page: ");
                if (page > 1) {
                    String prevText = isChinese ? "§e[上一页]" : "§e[Previous]";
                    int finalPage = page;
                    ((net.minecraft.text.MutableText) navigation).append(Text.literal(prevText)
                            .styled(style -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress list " + (finalPage - 1))
                            ).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal(isChinese ? "点击查看上一页" : "Click to view previous page"))
                            )));
                }

                for (int i = 1; i <= totalPages; i++) {
                    if (i == page) {
                        ((net.minecraft.text.MutableText) navigation).append(Text.literal(" §a[" + i + "] "));
                    } else {
                        int finalI = i;
                        ((net.minecraft.text.MutableText) navigation).append(Text.literal(" §7[" + i + "]")
                                .styled(style -> style.withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress list " + finalI)
                                ).withHoverEvent(
                                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Text.literal(isChinese ? "点击查看第" + finalI + "页" : "Click to view page " + finalI))
                                )));
                    }
                }

                if (page < totalPages) {
                    String nextText = isChinese ? "§e[下一页]" : "§e[Next]";
                    int finalPage1 = page;
                    ((net.minecraft.text.MutableText) navigation).append(Text.literal(nextText)
                            .styled(style -> style.withClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress list " + (finalPage1 + 1))
                            ).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal(isChinese ? "点击查看下一页" : "Click to view next page"))
                            )));
                }

                source.sendMessage(navigation);
            }

            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int viewProgress(CommandContext<ServerCommandSource> context, String progressId) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            List<ProgressManager.CustomProgress> progresses = ProgressManager.getPlayerData(player);
            ProgressManager.CustomProgress progress = progresses.stream().filter(p -> p.id.equals(progressId)).findFirst().orElse(null);

            if (progress == null) {
                String lang = getPlayerLanguage(player.getUuid());
                String errorMsg = lang.equals("zh_cn") ?
                        "§c✗ §7未找到ID为 §e" + progressId + " §7的进度" :
                        "§c✗ §7Progress not found with ID: §e" + progressId;
                source.sendMessage(Text.literal(errorMsg));
                return 0;
            }

            String lang = getPlayerLanguage(player.getUuid());
            boolean isChinese = lang.equals("zh_cn");

            String status = progress.completed ?
                    (isChinese ? "§a已完成" : "§aCompleted") :
                    (isChinese ? "§e进行中" : "§eIn Progress");
            String progressBar = createProgressBar(progress.current, progress.targetCount, 30);
            String percentage = String.format("%.1f%%", progress.getProgress() * 100);
            long createdTime = progress.createdTime;
            String timeAgo = formatTimeAgo(createdTime, isChinese);
            String typeDisplayName = getTypeDisplayName(progress.type, player.getUuid());

            String header = isChinese ? "§6=== 进度详情 ===" : "§6=== Progress Details ===";
            source.sendMessage(Text.literal(header));

            Text details = Text.literal("§f" + progress.title)
                    .append(Text.literal("\n§7" + (isChinese ? "状态: " : "Status: ") + status))
                    .append(Text.literal("\n§7" + (isChinese ? "类型: " : "Type: ") + progress.type.getColorCode() + typeDisplayName))
                    .append(Text.literal("\n§7" + (isChinese ? "目标: " : "Target: ") + "§a" + progress.target + " §7x§e" + progress.targetCount))
                    .append(Text.literal("\n§7" + (isChinese ? "进度: " : "Progress: ") + progressBar))
                    .append(Text.literal("\n§7" + (isChinese ? "完成度: " : "Completion: ") + "§e" + percentage + " §7(§e" + progress.current + "§7/§a" + progress.targetCount + "§7)"))
                    .append(Text.literal("\n§7ID: §e" + progress.id))
                    .append(Text.literal("\n§7" + (isChinese ? "创建时间: " : "Created: ") + "§7" + timeAgo));

            source.sendMessage(details);

            // 操作按钮
            Text actions = Text.literal("§7" + (isChinese ? "操作: " : "Actions: "));

            if (!progress.completed) {
                ((net.minecraft.text.MutableText) actions).append(Text.literal("§a[+1] ")
                        .styled(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/progress update " + progress.id + " " + (progress.current + 1))
                        ).withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal(isChinese ? "点击增加进度" : "Click to increase progress"))
                        )));

                ((net.minecraft.text.MutableText) actions).append(Text.literal("§6[+5] ")
                        .styled(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/progress update " + progress.id + " " + (progress.current + 5))
                        ).withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal(isChinese ? "点击增加5点进度" : "Click to add 5 progress"))
                        )));

                ((net.minecraft.text.MutableText) actions).append(Text.literal("§c[" + (isChinese ? "删除" : "Delete") + "] ")
                        .styled(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress delete " + progress.id)
                        ).withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal(isChinese ? "点击删除此进度" : "Click to delete this progress"))
                        )));
            } else {
                ((net.minecraft.text.MutableText) actions).append(Text.literal("§a[" + (isChinese ? "已完成" : "Completed") + "] "));
                ((net.minecraft.text.MutableText) actions).append(Text.literal("§c[" + (isChinese ? "删除" : "Delete") + "] ")
                        .styled(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/progress delete " + progress.id)
                        ).withHoverEvent(
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        Text.literal(isChinese ? "点击删除此进度" : "Click to delete this progress"))
                        )));
            }

            source.sendMessage(actions);

            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int updateProgress(CommandContext<ServerCommandSource> context, String progressId, int current) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            List<ProgressManager.CustomProgress> progresses = ProgressManager.getPlayerData(player);
            ProgressManager.CustomProgress progress = null;

            for (var p : progresses) {
                if (p.id.equals(progressId)) {
                    progress = p;
                    break;
                }
            }

            if (progress == null) {
                String lang = getPlayerLanguage(player.getUuid());
                String errorMsg = lang.equals("zh_cn") ?
                        "§c✗ §7未找到ID为 §e" + progressId + " §7的进度" :
                        "§c✗ §7Progress not found with ID: §e" + progressId;
                source.sendMessage(Text.literal(errorMsg));
                return 0;
            }

            int oldCurrent = progress.current;
            progress.current = Math.min(Math.max(0, current), progress.targetCount);
            progress.completed = progress.current >= progress.targetCount;

            // 保存更新
            ProgressManager.removeProgress(player, progressId);
            ProgressManager.addProgress(player, progress);

            String lang = getPlayerLanguage(player.getUuid());
            boolean isChinese = lang.equals("zh_cn");

            String status = progress.completed ?
                    (isChinese ? "§a已完成！" : "§aCompleted!") :
                    (isChinese ? "§e更新成功" : "§eUpdated successfully");
            String progressBar = createProgressBar(progress.current, progress.targetCount, 20);
            String percentage = String.format("%.1f%%", progress.getProgress() * 100);
            String typeDisplayName = getTypeDisplayName(progress.type, player.getUuid());

            Text message = Text.literal("§a✓ " + (isChinese ? "§7进度已更新: " : "§7Progress updated: ") + "§f" + progress.title)
                    .append(Text.literal("\n§7" + (isChinese ? "类型: " : "Type: ") + progress.type.getColorCode() + typeDisplayName))
                    .append(Text.literal("\n§7" + (isChinese ? "进度: " : "Progress: ") + progressBar + " §e" + percentage))
                    .append(Text.literal("\n§7" + (isChinese ? "完成: " : "Completed: ") + "§e" + progress.current + "§7/§a" + progress.targetCount))
                    .append(Text.literal("\n§7" + (isChinese ? "变化: " : "Change: ") + "§e" + oldCurrent + " §7→ §a" + progress.current))
                    .append(Text.literal("\n§7" + (isChinese ? "状态: " : "Status: ") + status));

            source.sendMessage(message);

            if (progress.completed) {
                String congrats = isChinese ?
                        "§a🎉 恭喜！你完成了进度: §f" + progress.title :
                        "§a🎉 Congratulations! You completed progress: §f" + progress.title;
                source.sendMessage(Text.literal(congrats));
            }

            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int deleteProgress(CommandContext<ServerCommandSource> context, String progressId) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            List<ProgressManager.CustomProgress> progresses = ProgressManager.getPlayerData(player);
            ProgressManager.CustomProgress progress = null;

            for (var p : progresses) {
                if (p.id.equals(progressId)) {
                    progress = p;
                    break;
                }
            }

            if (progress == null) {
                String lang = getPlayerLanguage(player.getUuid());
                String errorMsg = lang.equals("zh_cn") ?
                        "§c✗ §7未找到ID为 §e" + progressId + " §7的进度" :
                        "§c✗ §7Progress not found with ID: §e" + progressId;
                source.sendMessage(Text.literal(errorMsg));
                return 0;
            }

            ProgressManager.removeProgress(player, progressId);

            String lang = getPlayerLanguage(player.getUuid());
            String message = lang.equals("zh_cn") ?
                    "§a✓ §7已删除进度: §f" + progress.title :
                    "§a✓ §7Deleted progress: §f" + progress.title;

            source.sendMessage(Text.literal(message));
            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int clearProgresses(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            var progresses = ProgressManager.getPlayerData(player);

            String lang = getPlayerLanguage(player.getUuid());
            boolean isChinese = lang.equals("zh_cn");

            if (progresses.isEmpty()) {
                String message = isChinese ?
                        "§a[SimpleProgress] §7你没有任何进度记录可清除" :
                        "§a[SimpleProgress] §7You don't have any progress records to clear";
                source.sendMessage(Text.literal(message));
                return 1;
            }

            source.sendMessage(Text.literal("§c⚠ " + (isChinese ? "§7警告：此操作将清除所有进度数据！" : "§7Warning: This will clear all progress data!")));
            source.sendMessage(Text.literal("§7" + (isChinese ? "当前有 §e" : "You have §e") + progresses.size() + (isChinese ? " §7个进度记录" : " §7progress records")));
            source.sendMessage(Text.literal("§7" + (isChinese ? "使用 §e/progress clear confirm §7来确认清除" : "Use §e/progress clear confirm §7to confirm")));
            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int confirmClearProgresses(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            var progresses = ProgressManager.getPlayerData(player);
            int count = progresses.size();

            ProgressManager.clearAllProgresses(player);

            String lang = getPlayerLanguage(player.getUuid());
            String message = lang.equals("zh_cn") ?
                    "§a✓ §7已清除 §e" + count + " §7个进度记录" :
                    "§a✓ §7Cleared §e" + count + " §7progress records";

            source.sendMessage(Text.literal(message));
            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int showStats(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            var progresses = ProgressManager.getPlayerData(player);

            int total = progresses.size();
            int completed = 0;
            int killCount = 0, obtainCount = 0, buildCount = 0;
            int totalProgress = 0, totalTarget = 0;

            for (var progress : progresses) {
                if (progress.completed) completed++;

                switch (progress.type) {
                    case KILL: killCount++; break;
                    case OBTAIN: obtainCount++; break;
                    case BUILD: buildCount++; break;
                }

                totalProgress += progress.current;
                totalTarget += progress.targetCount;
            }

            float completionRate = total > 0 ? (float) completed / total * 100 : 0;
            float overallProgress = totalTarget > 0 ? (float) totalProgress / totalTarget * 100 : 0;

            String lang = getPlayerLanguage(player.getUuid());
            boolean isChinese = lang.equals("zh_cn");

            String header = isChinese ? "§6=== 进度统计 ===" : "§6=== Progress Statistics ===";
            source.sendMessage(Text.literal(header));

            source.sendMessage(Text.literal("§7" + (isChinese ? "总进度数: " : "Total Progresses: ") + "§e" + total));
            source.sendMessage(Text.literal("§7" + (isChinese ? "已完成: " : "Completed: ") + "§a" + completed +
                    " §7(§e" + String.format("%.1f", completionRate) + "%§7)"));
            source.sendMessage(Text.literal("§7" + (isChinese ? "总进度: " : "Total Progress: ") + "§e" + totalProgress + "§7/§a" + totalTarget +
                    " §7(§e" + String.format("%.1f", overallProgress) + "%§7)"));
            source.sendMessage(Text.literal("§7" + (isChinese ? "类型分布:" : "Type Distribution:")));
            source.sendMessage(Text.literal("  §c" + (isChinese ? "击杀: " : "Kill: ") + "§7" + killCount));
            source.sendMessage(Text.literal("  §a" + (isChinese ? "获得: " : "Obtain: ") + "§7" + obtainCount));
            source.sendMessage(Text.literal("  §6" + (isChinese ? "建筑: " : "Build: ") + "§7" + buildCount));

            // 进度排行榜
            if (total > 0) {
                source.sendMessage(Text.literal("§7" + (isChinese ? "进度排名:" : "Top Progresses:")));

                // 找到进度最接近完成的3个
                progresses.sort((a, b) -> {
                    float aRatio = a.getProgress();
                    float bRatio = b.getProgress();
                    return Float.compare(bRatio, aRatio); // 降序排列
                });

                int showCount = Math.min(3, progresses.size());
                for (int i = 0; i < showCount; i++) {
                    var progress = progresses.get(i);
                    String ranking;
                    if (i == 0) ranking = "🥇";
                    else if (i == 1) ranking = "🥈";
                    else ranking = "🥉";

                    source.sendMessage(Text.literal("  " + ranking + " §f" + progress.title +
                            " §7(§e" + String.format("%.1f", progress.getProgress() * 100) + "%§7)"));
                }
            }

            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    // 语言相关命令
    private static int showCurrentLanguage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            String lang = getPlayerLanguage(player.getUuid());
            String currentLangName = lang.equals("zh_cn") ? "简体中文" : "English (US)";
            String message = "§a[SimpleProgress] §7当前语言: §e" + currentLangName + " §7(" + lang + ")";
            source.sendMessage(Text.literal(message));
            source.sendMessage(Text.literal("§7使用 §e/progress lang en_us §7切换为英文"));
            source.sendMessage(Text.literal("§7使用 §e/progress lang zh_cn §7切换为中文"));
            source.sendMessage(Text.literal("§7使用 §e/progress lang reset §7重置为系统默认"));
            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int setLanguage(CommandContext<ServerCommandSource> context, String language) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            playerLanguages.put(player.getUuid(), language);

            String langName = language.equals("zh_cn") ? "简体中文" : "English (US)";
            String message = language.equals("zh_cn") ?
                    "§a✓ §7语言已设置为 §e简体中文 §7(zh_cn)" :
                    "§a✓ §7Language set to §eEnglish (US) §7(en_us)";

            source.sendMessage(Text.literal(message));
            source.sendMessage(Text.literal("§7" + (language.equals("zh_cn") ?
                    "现在所有进度命令将显示中文界面" :
                    "All progress commands will now display in English")));

            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    private static int resetLanguage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            playerLanguages.remove(player.getUuid());

            // 获取系统默认语言
            String systemLang = LanguageManager.getCurrentLanguage();
            String langName = systemLang.equals("zh_cn") ? "简体中文" : "English (US)";

            String message = systemLang.equals("zh_cn") ?
                    "§a✓ §7语言已重置为系统默认 §e简体中文" :
                    "§a✓ §7Language reset to system default §eEnglish (US)";

            source.sendMessage(Text.literal(message));
            return 1;
        }

        source.sendMessage(Text.literal("§c只有玩家可以使用此命令"));
        return 0;
    }

    // 辅助方法
    private static String createProgressBar(int current, int target, int length) {
        float percentage = target > 0 ? (float) current / target : 0;
        int filled = (int) (percentage * length);
        int empty = length - filled;

        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }

        bar.append("§7");
        for (int i = 0; i < empty; i++) {
            bar.append("░");
        }

        return bar.toString();
    }

    private static String formatTimeAgo(long timestamp, boolean isChinese) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // 小于1分钟
            long seconds = diff / 1000;
            return seconds + (isChinese ? "秒前" : " seconds ago");
        } else if (diff < 3600000) { // 小于1小时
            long minutes = diff / 60000;
            return minutes + (isChinese ? "分钟前" : " minutes ago");
        } else if (diff < 86400000) { // 小于1天
            long hours = diff / 3600000;
            return hours + (isChinese ? "小时前" : " hours ago");
        } else {
            long days = diff / 86400000;
            return days + (isChinese ? "天前" : " days ago");
        }
    }

    private static String getTypeDisplayName(ProgressManager.ProgressType type, UUID playerId) {
        String lang = getPlayerLanguage(playerId);
        return LanguageManager.getTranslation("progress.type." + type.name().toLowerCase(), lang);
    }

    private static String getPlayerLanguage(UUID playerId) {
        // 如果玩家设置了语言偏好，使用该偏好
        if (playerLanguages.containsKey(playerId)) {
            return playerLanguages.get(playerId);
        }
        // 否则使用系统默认语言
        return LanguageManager.getCurrentLanguage();
    }

    private static void sendHelpMessage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        // 获取玩家语言偏好
        String lang = "en_us";
        if (source.isExecutedByPlayer()) {
            ServerPlayerEntity player = source.getPlayer();
            if (player != null) {
                lang = getPlayerLanguage(player.getUuid());
            }
        }

        boolean isChinese = lang.equals("zh_cn");

        String header = isChinese ? "§6=== SimpleProgress 命令帮助 ===" : "§6=== SimpleProgress Command Help ===";
        source.sendMessage(Text.literal(header));

        source.sendMessage(Text.literal("§e/progress help §7- " + (isChinese ? "显示此帮助信息" : "Show this help message")));
        source.sendMessage(Text.literal("§e/progress add <标题> §7- " + (isChinese ? "添加进度" : "Add progress")));
        source.sendMessage(Text.literal("  §7/progress add <标题> <目标> <数量> <kill|obtain|build>"));
        source.sendMessage(Text.literal("  §7" + (isChinese ? "示例: " : "Example: ") + "/progress add " +
                (isChinese ? "杀僵尸" : "Kill Zombies") + " minecraft:zombie 50 kill"));
        source.sendMessage(Text.literal("§e/progress list [页码] §7- " + (isChinese ? "列出所有进度" : "List all progresses")));
        source.sendMessage(Text.literal("§e/progress view <ID> §7- " + (isChinese ? "查看进度详情" : "View progress details")));
        source.sendMessage(Text.literal("§e/progress update <ID> <数量> §7- " + (isChinese ? "更新进度" : "Update progress")));
        source.sendMessage(Text.literal("§e/progress delete <ID> §7- " + (isChinese ? "删除进度" : "Delete progress")));
        source.sendMessage(Text.literal("§e/progress clear §7- " + (isChinese ? "清除所有进度" : "Clear all progresses")));
        source.sendMessage(Text.literal("§e/progress stats §7- " + (isChinese ? "查看统计信息" : "Show statistics")));
        source.sendMessage(Text.literal("§e/progress lang §7- " + (isChinese ? "语言设置" : "Language settings")));
        source.sendMessage(Text.literal("  §7/progress lang en_us §7- " + (isChinese ? "切换为英文" : "Switch to English")));
        source.sendMessage(Text.literal("  §7/progress lang zh_cn §7- " + (isChinese ? "切换为中文" : "Switch to Chinese")));
        source.sendMessage(Text.literal("  §7/progress lang reset §7- " + (isChinese ? "重置为默认" : "Reset to default")));
        source.sendMessage(Text.literal("§e/prog list §7- " + (isChinese ? "快捷列出进度" : "Quick list progresses")));
        source.sendMessage(Text.literal("§7" + (isChinese ? "版本: " : "Version: ") + "§a1.0.3 §7| " +
                (isChinese ? "开发者: " : "Developer: ") + "§e澜澈LanChe"));

        // 显示玩家当前进度数量
        if (source.isExecutedByPlayer()) {
            var player = source.getPlayer();
            if (player != null) {
                var progresses = ProgressManager.getPlayerData(player);
                int completed = 0;
                for (var p : progresses) {
                    if (p.completed) completed++;
                }
                String progressText = isChinese ?
                        "§7你的进度: §a" + completed + "§7/§e" + progresses.size() + " §7已完成" :
                        "§7Your progress: §a" + completed + "§7/§e" + progresses.size() + " §7completed";
                source.sendMessage(Text.literal(progressText));

                // 显示当前语言
                String currentLang = getPlayerLanguage(player.getUuid());
                String langText = currentLang.equals("zh_cn") ? "简体中文" : "English";
                source.sendMessage(Text.literal("§7" + (isChinese ? "当前语言: " : "Current language: ") + "§e" + langText));
            }
        }
    }
}