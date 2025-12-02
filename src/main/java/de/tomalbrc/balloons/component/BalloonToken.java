package de.tomalbrc.balloons.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.util.TextUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record BalloonToken(
        @NotNull ResourceLocation id,
        @Nullable String permission,
        @Nullable Integer permissionLevel
) implements TooltipProvider {
    public static final Codec<BalloonToken> CODEC = RecordCodecBuilder.create(instance -> instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(BalloonToken::id), Codec.STRING.optionalFieldOf("permission").forGetter(token -> java.util.Optional.ofNullable(token.permission())), Codec.INT.optionalFieldOf("permission_level").forGetter(token -> java.util.Optional.ofNullable(token.permissionLevel()))).apply(instance, (id, permissionOpt, levelOpt) -> new BalloonToken(id, permissionOpt.orElse(null), levelOpt.orElse(null))));

    public boolean canUse(ServerPlayer player) {
        if (permission == null) return permissionLevel == null || player.hasPermissions(permissionLevel);
        return Permissions.check(player, permission, permissionLevel == null ? 0 : permissionLevel);
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        var b = Balloons.all().get(this.id);
        if (b != null)
            consumer.accept(TextUtil.parse(String.format(ModConfig.getInstance().messages.componentTooltip, b.title())));
    }
}
