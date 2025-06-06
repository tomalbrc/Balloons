package de.tomalbrc.balloons.config;

import de.tomalbrc.balloons.BalloonComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ModConfigBalloon {
    ResourceLocation id;
    @Nullable ItemStack item;
    BalloonComponent data;

    public ModConfigBalloon(ResourceLocation id, @Nullable ItemStack item, BalloonComponent data) {
        this.id = id;
        this.item = item;
        this.data = data;
    }

    public void setItem(@Nullable ItemStack item) {
        this.item = item;
    }

    public BalloonComponent data() {
        return data;
    }

    public ItemStack item() {
        return item;
    }

    public ResourceLocation id() {
        return id;
    }
}
