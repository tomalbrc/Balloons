package de.tomalbrc.balloons.mixin;

import de.tomalbrc.balloons.util.ClientboundSetEntityLinkPacketExt;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientboundSetEntityLinkPacket.class)
public class ClientboundSetEntityLinkPacketMixin implements ClientboundSetEntityLinkPacketExt {
    @Unique int customId = -1;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeInt(I)Lnet/minecraft/network/FriendlyByteBuf;", ordinal = 0))
    private int balloons$injectBalloonLeash(int value) {
        if (customId != -1) {
            return customId;
        }
        return value;
    }

    @Override
    public void balloons$setCustomId(int id) {
        this.customId = id;
    }
}
