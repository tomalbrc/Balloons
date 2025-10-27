package de.tomalbrc.balloons.impl;

import de.tomalbrc.balloons.gui.SelectionGui;
import de.tomalbrc.balloons.util.ClientboundSetEntityLinkPacketExt;
import de.tomalbrc.bil.core.holder.base.AbstractAnimationHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.List;

public class AnimatedBalloonHolder extends AbstractAnimationHolder {
    private final GenericEntityElement leashElement;
    private float yaw, pitch;
    private final boolean leash;

    protected AnimatedBalloonHolder(Model model, boolean leash) {
        super(model);
        this.leash = leash;
        this.leashElement = new BalloonRootElement();
        this.leashElement.setInteractionHandler(new VirtualElement.InteractionHandler() {
            @Override
            public void interact(ServerPlayer player, InteractionHand hand) {
                var gui = new SelectionGui(player, false);
                gui.open();
            }
        });
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

            if (this.leash) {
                var packet = new ClientboundSetEntityLinkPacket(player.player, player.player);
                ((ClientboundSetEntityLinkPacketExt)packet).balloons$setCustomId(this.leashElement.getEntityId());
                list.add(packet);
            }

            var attributeInstance = new AttributeInstance(Attributes.SCALE, (instance) -> {});
            attributeInstance.setBaseValue(0.2);
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
    public void updateElement(ServerPlayer player, DisplayWrapper<?> display, @Nullable Pose pose) {
        if (pose != null) {
            this.applyPose(null, pose, display);
        } else {
            this.applyPose(null, display.getDefaultPose(), display);
        }
    }

    @Override
    protected void applyPose(ServerPlayer player, Pose pose, DisplayWrapper<?> display) {
        Matrix4f matrix = compose(pose.readOnlyTranslation(), pose.readOnlyLeftRotation(), pose.readOnlyScale(), pose.readOnlyRightRotation());

        Matrix4f m = new Matrix4f().rotateLocalX((float) Math.toRadians(-pitch)).rotateLocalY((float) Math.toRadians(-yaw)).mul(matrix);
        m.translateLocal(0, -0.1f, 0);
        display.element().setTransformation(null, m);
        display.element().startInterpolationIfDirty(null);
    }

    @Override
    public CommandSourceStack createCommandSourceStack() {
        String name = String.format("BalloonHolder[%.1f, %.1f, %.1f]", this.getPos().x, this.getPos().y, this.getPos().z);
        return new CommandSourceStack(
                this.getLevel().getServer(),
                this.getPos(),
                Vec2.ZERO,
                this.getLevel(),
                0,
                name,
                Component.literal(name),
                this.getLevel().getServer(),
                null
        );
    }

    private static Matrix4f compose(@Nullable Vector3fc vector3f, @Nullable Quaternionfc quaternionf, @Nullable Vector3fc vector3f2, @Nullable Quaternionfc quaternionf2) {
        Matrix4f matrix4f = new Matrix4f();
        if (vector3f != null) {
            matrix4f.translation(vector3f);
        }

        if (quaternionf != null) {
            matrix4f.rotate(quaternionf);
        }

        if (vector3f2 != null) {
            matrix4f.scale(vector3f2);
        }

        if (quaternionf2 != null) {
            matrix4f.rotate(quaternionf2);
        }

        return matrix4f;
    }

    private static class BalloonRootElement extends GenericEntityElement {
        public BalloonRootElement() {
            super();

            this.dataTracker.set(EntityTrackedData.SILENT, true);
            this.dataTracker.set(EntityTrackedData.NO_GRAVITY, true);
            this.dataTracker.set(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
        }

        @Override
        protected EntityType<? extends Entity> getEntityType() {
            return EntityType.TROPICAL_FISH;
        }


    }
}
