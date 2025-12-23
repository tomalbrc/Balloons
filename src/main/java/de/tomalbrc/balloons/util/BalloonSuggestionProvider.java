package de.tomalbrc.balloons.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ConfiguredBalloon;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.IdentifierArgument;

import java.util.concurrent.CompletableFuture;

public class BalloonSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        if (!context.getInput().endsWith(" ")) {
            var res = IdentifierArgument.getId(context, "id");
            for (ConfiguredBalloon balloon : Balloons.all().values()) {
                if (balloon.id().toString().contains(res.getPath()))
                    builder.suggest(balloon.id().toString());
            }
        } else {
            for (ConfiguredBalloon balloon : Balloons.all().values()) {
                builder.suggest(balloon.id().toString());
            }
        }
        return builder.buildFuture();
    }
}
