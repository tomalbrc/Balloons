package de.tomalbrc.balloons.impl;

import de.tomalbrc.bil.core.element.CollisionElement;
import de.tomalbrc.bil.core.holder.base.SimpleAnimatedHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class AnimatedBalloonHolder extends SimpleAnimatedHolder {
    private final GenericEntityElement leashElement;
    private float yaw, pitch;
    private final boolean leash;

    protected AnimatedBalloonHolder(Model model, boolean leash) {
        super(model);
        this.leash = leash;
        var slime =  new CollisionElement(VirtualElement.InteractionHandler.EMPTY);
        slime.setSize(1);
        this.leashElement = slime;
        this.addElement(this.leashElement);
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl player) {
        var value = super.startWatching(player);
        if (value) {
            var ids = new IntArrayList();
            for (Bone<?> bone : this.bones) {
                ids.add(bone.element().getEntityId());
            }


            var ridePacket = VirtualEntityUtils.createRidePacket(this.leashElement.getEntityId(), ids);
            var list = ObjectArrayList.<Packet<? super ClientGamePacketListener>>of(ridePacket);

            if (this.leash)
                list.add(new ClientboundSetEntityLinkPacket(player.player, player.player));

            var attributeInstance = new AttributeInstance(Attributes.SCALE, (instance) -> {});
            attributeInstance.setBaseValue(0.01);
            var attributesPacket = new ClientboundUpdateAttributesPacket(this.leashElement.getEntityId(), List.of(attributeInstance));
            list.add(attributesPacket);

            player.send(new ClientboundBundlePacket(list));
        }

        return value;
    }

    public void setPosition(Vec3 position) {
        this.leashElement.setOverridePos(position);
    }

    public int leashedEntityId() {
        return this.leashElement.getEntityId();
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Override
    public void updateElement(DisplayWrapper<?> display, @Nullable Pose pose) {
        if (pose != null) {
            this.applyPose(pose, display);
        } else {
            this.applyPose(display.getDefaultPose(), display);
        }
    }

    @Override
    protected void applyPose(Pose pose, DisplayWrapper<?> display) {
        var m = new Matrix4f().rotateLocalX((float) Math.toRadians(-pitch)).rotateLocalY((float) Math.toRadians(-yaw)).mul(pose.matrix());
        m.translateLocal(0, -0.1f, 0);
        display.element().setTransformation(m);
        display.element().startInterpolationIfDirty();
    }
}
