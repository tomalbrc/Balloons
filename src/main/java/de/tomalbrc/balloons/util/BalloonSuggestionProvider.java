package de.tomalbrc.balloons.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.tomalbrc.balloons.Balloons;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;

import java.util.concurrent.CompletableFuture;

public class BalloonSuggestionProvider implements SuggestionProvider<CommandSourceStack> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        var res = ResourceLocationArgument.getId(context, "id");
        for (ModConfig.ConfigBalloon balloon : Balloons.REGISTERED_BALLOONS.values()) {
            if (balloon.id().toString().contains(res.getPath()))builder.suggest(balloon.id().toString());
        }
        return builder.buildFuture();
    }
}
