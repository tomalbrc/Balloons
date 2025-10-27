package de.tomalbrc.balloons.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.component.ModComponents;
import de.tomalbrc.balloons.config.ModConfig;
import de.tomalbrc.balloons.util.TextUtil;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V", at = @At("TAIL"))
    private void balloons$onTeleport(CallbackInfo ci) {
        Balloons.spawnActive(player);
    }

    @Inject(method = "handleUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void balloons$onUse(ServerboundUseItemPacket serverboundUseItemPacket, CallbackInfo ci, @Local ItemStack itemStack) {
        if (itemStack.has(ModComponents.TOKEN)) {
            var tokenId = itemStack.get(ModComponents.TOKEN);
            if (tokenId != null && tokenId.canUse(player) && Balloons.getStorage().add(player.getUUID(), tokenId.id())) {
                String title = Balloons.all().get(tokenId.id()).title();
                player.sendSystemMessage(TextUtil.parse(String.format(ModConfig.getInstance().messages.added, title == null ? tokenId.id() : title)));
                itemStack.consume(1, player);
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void balloons$onUseOn(ServerboundUseItemOnPacket serverboundUseItemOnPacket, CallbackInfo ci, @Local ItemStack itemStack) {
        if (itemStack.has(ModComponents.TOKEN)) {
            var tokenId = itemStack.get(ModComponents.TOKEN);
            if (tokenId != null && tokenId.canUse(player) && Balloons.getStorage().add(player.getUUID(), tokenId.id())) {
                String title = Balloons.all().get(tokenId.id()).title();
                player.sendSystemMessage(TextUtil.parse(String.format(ModConfig.getInstance().messages.added, title == null ? tokenId.id() : title)));
                itemStack.consume(1, player);
            }
            ci.cancel();
        }
    }
}
