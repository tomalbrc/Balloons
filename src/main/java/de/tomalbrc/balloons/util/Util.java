package de.tomalbrc.balloons.util;

import de.tomalbrc.balloons.BalloonFenceLeashKnot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class Util {
    public static MenuType<?> menuTypeForHeight(int height) {
        return switch (height) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            case 6 -> MenuType.GENERIC_9x6;
            default -> MenuType.GENERIC_3x3;
        };
    }

    public static void clickSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER, 0.5f, 1F);
    }

    public static BalloonFenceLeashKnot createKnot(Level level, BlockPos blockPos) {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        for (BalloonFenceLeashKnot entity : level.getEntitiesOfClass(BalloonFenceLeashKnot.class, new AABB(x - 1.0F, y - 1.0F, z - 1.0F, x + 1.0F, y + 1.0F, z + 1.0F))) {
            if (entity.getPos().equals(blockPos)) {
                return null;
            }
        }

        BalloonFenceLeashKnot leashFenceKnotEntity2 = new BalloonFenceLeashKnot(level, blockPos);
        level.addFreshEntity(leashFenceKnotEntity2);
        return leashFenceKnotEntity2;
    }
}
