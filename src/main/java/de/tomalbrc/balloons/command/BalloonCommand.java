package de.tomalbrc.balloons.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.gui.SelectionGui;
import de.tomalbrc.balloons.util.StorageUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BalloonCommand {
    private BalloonCommand() {}

    public static void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {
        ModConfig config = ModConfig.getInstance();

        SuggestionProvider<CommandSourceStack> balloonSuggestions = (context, builder) -> {
            String rem = builder.getRemaining().toLowerCase(Locale.ROOT);
            for (var entry : Balloons.all().entrySet()) {
                String idStr = entry.toString();
                if (idStr.toLowerCase(Locale.ROOT).startsWith(rem)) {
                    builder.suggest(entry.getKey().toString());
                }
            }
            return builder.buildFuture();
        };

        Function<String, Predicate<CommandSourceStack>> requirePerm =
                permKey -> Permissions.require(permKey, config.permissions.getOrDefault(permKey, 2));

        var root = literal("balloons")
                .requires(requirePerm.apply("balloons.command"))
                .executes(BalloonCommand::openGui);

        // /balloons reload
        root = root.then(literal("reload")
                .requires(requirePerm.apply("balloons.reload"))
                .executes(ctx -> {
                    ModConfig.load();
                    ctx.getSource().sendSuccess(() -> Component.literal("Balloons config reloaded."), false);
                    return Command.SINGLE_SUCCESS;
                }));

        // /balloons activate <id>
        root = root.then(literal("activate")
                .requires(requirePerm.apply("balloons.activate"))
                .then(argument("id", ResourceLocationArgument.id())
                        .suggests(balloonSuggestions)
                        .executes(BalloonCommand::handleActivate)));

        // /balloons hide
        root = root.then(literal("hide")
                .requires(requirePerm.apply("balloons.hide"))
                .executes(BalloonCommand::handleHide));

        // /balloons give <player> <balloon>
        root = root.then(literal("give")
                .requires(requirePerm.apply("balloons.give"))
                .then(argument("player", EntityArgument.player())
                        .then(literal("*").executes(BalloonCommand::handleGiveAll))
                        .then(argument("balloon", StringArgumentType.word())
                                .suggests(balloonSuggestions)
                                .executes(BalloonCommand::handleGive))));

        // /balloons remove <player> <balloon>
        root = root.then(literal("remove")
                .requires(requirePerm.apply("balloons.remove"))
                .then(argument("player", EntityArgument.player())
                        .then(literal("*").executes(BalloonCommand::handleRemoveAll))
                        .then(argument("balloon", StringArgumentType.word())
                                .suggests(balloonSuggestions)
                                .executes(BalloonCommand::handleRemove))));

        // /balloons list <player>
        root = root.then(literal("list")
                .requires(requirePerm.apply("balloons.list"))
                .then(argument("player", EntityArgument.player())
                        .executes(BalloonCommand::handleList)));

        // /balloons <balloon>
        dispatcher.register(root.then(argument("balloon", StringArgumentType.word())
                .requires(requirePerm.apply("balloons.direct"))
                .suggests(balloonSuggestions)
                .executes(BalloonCommand::executeDirect)));

        dispatcher.register(root);
    }

    private static int openGui(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        SelectionGui gui = new SelectionGui(player, false);
        boolean opened = gui.open();

        return opened ? Command.SINGLE_SUCCESS : 0;
    }

    private static int handleActivate(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        var id = ResourceLocationArgument.getId(ctx, "id");
        boolean success = StorageUtil.setActive(player, id);
        if (success) {
            Balloons.spawnActive(player);
            ctx.getSource().sendSuccess(() -> Component.literal("Activated balloon " + id), false);
            return Command.SINGLE_SUCCESS;
        } else {
            ctx.getSource().sendFailure(Component.literal("You don’t own that balloon or cannot activate it."));
            return 0;
        }
    }

    private static int handleHide(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        StorageUtil.removeActive(player);
        Balloons.despawnBalloon(player);
        ctx.getSource().sendSuccess(() -> Component.literal("Balloon hidden."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleGiveAll(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            CompletableFuture.runAsync(() -> {
                int count = (int) Balloons.all().keySet().stream().filter(id -> StorageUtil.add(target.getUUID(), id)).count();
                ctx.getSource().sendSuccess(() -> Component.literal("Gave " + count + " balloons to " + target.getScoreboardName()), false);
            });
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("Failed to give all balloons."));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int handleGive(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            String balloonName = StringArgumentType.getString(ctx, "balloon");
            var id = ResourceLocation.tryParse(balloonName);
            if (id != null && StorageUtil.add(target.getUUID(), id)) {
                CompletableFuture.runAsync(() -> {
                    if (StorageUtil.add(target.getUUID(), id)) {
                        ctx.getSource().sendSuccess(() -> Component.literal("Gave balloon " + balloonName + " to " + target.getScoreboardName()), false);
                    } else {
                        ctx.getSource().sendFailure(Component.literal("Failed to give balloon (maybe target already has it)"));
                    }
                });
            }
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("Error in give command"));
            return 0;
        }
    }

    private static int handleRemoveAll(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            CompletableFuture.runAsync(() -> {
                int count = (int) Balloons.all().keySet().stream().filter(id -> StorageUtil.remove(target.getUUID(), id)).count();
                ctx.getSource().sendSuccess(() -> Component.literal("Removed " + count + " balloons from " + target.getScoreboardName()), false);
            });
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("Failed to remove all balloons."));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int handleRemove(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            String balloonName = StringArgumentType.getString(ctx, "balloon");
            var id = ResourceLocation.tryParse(balloonName);
            if (id != null && StorageUtil.remove(target.getUUID(), id)) {
                ctx.getSource().sendSuccess(() -> Component.literal("Removed balloon " + balloonName + " from " + target.getScoreboardName()), false);
                return Command.SINGLE_SUCCESS;
            } else {
                ctx.getSource().sendFailure(Component.literal("Failed to remove balloon (maybe target doesn’t own it)"));
                return 0;
            }
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("Error in remove command"));
            return 0;
        }
    }

    private static int handleList(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
            CompletableFuture.runAsync(() -> {
                Collection<ResourceLocation> list = StorageUtil.list(target.getUUID());
                if (list.isEmpty()) {
                    ctx.getSource().sendFailure(Component.literal("No balloons for player " + target.getScoreboardName()));
                } else {
                    for (ResourceLocation s : list) {
                        ctx.getSource().sendSuccess(() -> Component.literal(s.toString()), false);
                    }
                }
            });
            return Command.SINGLE_SUCCESS;
        } catch (Exception ex) {
            ctx.getSource().sendFailure(Component.literal("Error listing balloons"));
            return 0;
        }
    }

    private static int executeDirect(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        String balloonName = StringArgumentType.getString(ctx, "balloon");
        var id = ResourceLocation.tryParse(balloonName);
        if (id == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown balloon: " + balloonName));
            return 0;
        }

        boolean owns = StorageUtil.owns(player, id);
        if (!owns) {
            ctx.getSource().sendFailure(Component.literal("You don’t own balloon: " + balloonName));
            return 0;
        }

        // activate
        StorageUtil.setActive(player, id);
        Balloons.spawnActive(player);
        ctx.getSource().sendSuccess(() -> Component.literal("Activated balloon " + balloonName), false);
        return Command.SINGLE_SUCCESS;
    }
}
