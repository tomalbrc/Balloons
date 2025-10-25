package de.tomalbrc.balloons.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.util.BalloonSuggestionProvider;
import de.tomalbrc.balloons.util.StorageUtil;
import de.tomalbrc.bil.util.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BalloonCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var rootNode = Commands.literal("balloons").requires(Permissions.require("balloons.command", 2))
                .then(literal("reload").executes(ctx -> {
                    ModConfig.load();
                    return Command.SINGLE_SUCCESS;
                }))
                .then(literal("activate")
                        .then(argument("id", ResourceLocationArgument.id()).suggests(new BalloonSuggestionProvider()).executes(ctx -> {
                            var id = ResourceLocationArgument.getId(ctx, "id");
                            var player = ctx.getSource().getPlayer();
                            if (player != null) {
                                StorageUtil.setActive(ctx.getSource().getPlayer(), id);

                                Balloons.removeAllBalloons(player);
                                Balloons.addBalloon(player, id);

                                return Command.SINGLE_SUCCESS;
                            }
                            return 0;
                        })))
                .then(literal("hide")
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayer();
                            if (player != null) {
                                StorageUtil.removeActive(player);
                                Balloons.removeAllBalloons(player);
                                return Command.SINGLE_SUCCESS;
                            }

                            return 0;
                        }));

        dispatcher.register(rootNode);
    }
}
