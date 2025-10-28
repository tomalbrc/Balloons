package de.tomalbrc.balloons.impl;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PlayerAttachment {
    public static EntityAttachment ofTicking(ElementHolder holder, Entity entity) {
        var p = new EntityAttachment(holder, entity, true);
        if (entity instanceof ServerPlayer serverPlayer) holder.startWatching(serverPlayer);
        return p;
    }
}
