package de.tomalbrc.balloons.mixin;

import de.tomalbrc.balloons.Balloons;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundSetEntityLinkPacket.class)
public class ClientboundSetEntityLinkPacketMixin {
    @Mutable
    @Shadow @Final private int sourceId;

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void balloons$injectBalloonLeash(Entity source, Entity destination, CallbackInfo ci) {
        if (destination == source && source instanceof ServerPlayer serverPlayer) {
            var balloon = Balloons.ACTIVE_BALLOONS.get(serverPlayer);
            if (balloon != null) {
                this.sourceId = balloon.getLeashedEntityId();
            }
        }
    }
}
