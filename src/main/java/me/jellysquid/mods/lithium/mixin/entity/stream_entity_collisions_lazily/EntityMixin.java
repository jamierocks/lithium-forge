package me.jellysquid.mods.lithium.mixin.entity.stream_entity_collisions_lazily;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(Entity.class)
public class EntityMixin {
    /**
     * Redirect to try to collide with blocks first, so the entity stream doesn't have to be used when block collisions cancel the whole movement already.
     */
    @Redirect(
            method = "collideBoundingBoxHeuristically(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/world/World;Lnet/minecraft/util/math/shapes/ISelectionContext;Lnet/minecraft/util/ReuseableStream;)Lnet/minecraft/util/math/vector/Vector3d;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;concat(Ljava/util/stream/Stream;Ljava/util/stream/Stream;)Ljava/util/stream/Stream;"
            )
    )
    private static Stream<VoxelShape> reorderStreams_BlockCollisionsFirst(Stream<? extends VoxelShape> entityShapes, Stream<? extends VoxelShape> blockShapes) {
        return Stream.concat(blockShapes, entityShapes);
    }
}
