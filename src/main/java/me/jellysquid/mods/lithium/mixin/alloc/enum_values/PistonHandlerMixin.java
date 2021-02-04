package me.jellysquid.mods.lithium.mixin.alloc.enum_values;

import net.minecraft.block.PistonBlockStructureHelper;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonBlockStructureHelper.class)
public class PistonHandlerMixin {
    private static final Direction[] VALUES = Direction.values();

    @Redirect(
            method = "addBranchingBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Direction;values()[Lnet/minecraft/util/Direction;"
            )
    )
    private Direction[] redirectCanMoveAdjacentBlockValues() {
        return VALUES;
    }
}
