package me.jellysquid.mods.lithium.mixin.math.fast_util;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;
import net.minecraft.util.Direction;

@Mixin(Direction.class)
public class DirectionMixin {
    @Shadow
    @Final
    private static Direction[] VALUES;

    @Shadow
    @Final
    private int opposite;

    /**
     * @reason Avoid the modulo/abs operations
     * @author JellySquid
     */
    @Overwrite
    public Direction getOpposite() {
        return VALUES[this.opposite];
    }

    /**
     * @reason Do not allocate an excessive number of Direction arrays
     * @author JellySquid
     */
    @Overwrite
    public static Direction getRandomDirection(Random rand) {
        return VALUES[rand.nextInt(VALUES.length)];
    }
}
