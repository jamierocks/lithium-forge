package me.jellysquid.mods.lithium.common.entity.movement;

import me.jellysquid.mods.lithium.common.shapes.VoxelShapeCaster;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;

import static me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions.EPSILON;

public class BlockCollisionSweeper {
    private final BlockPos.Mutable mpos = new BlockPos.Mutable();

    /**
     * The collision box being swept through the world.
     */
    private final AxisAlignedBB box;

    /**
     * The VoxelShape of the collision box being swept through the world.
     */
    private final VoxelShape shape;

    private final ICollisionReader view;
    private final ISelectionContext context;
    private final CubeCoordinateIterator cuboidIt;

    private VoxelShape collidedShape;

    public BlockCollisionSweeper(ICollisionReader view, Entity entity, AxisAlignedBB box) {
        this.box = box;
        this.shape = VoxelShapes.create(box);
        this.context = entity == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(entity);
        this.view = view;
        this.cuboidIt = createVolumeIteratorForCollision(box);
    }

    /**
     * Advances the sweep forward by one block, updating the return value of
     * {@link BlockCollisionSweeper#getCollidedShape()} with a block shape if the sweep collided with it.
     *
     * @return True if there are blocks left to be tested, otherwise false
     */
    public boolean step() {
        this.collidedShape = null;

        final CubeCoordinateIterator cuboidIt = this.cuboidIt;

        if (!cuboidIt.hasNext()) {
            return false;
        }

        final int edgesHit = cuboidIt.numBoundariesTouched();

        if (edgesHit == 3) {
            return true;
        }

        final int x = cuboidIt.getX();
        final int y = cuboidIt.getY();
        final int z = cuboidIt.getZ();

        final IBlockReader chunk = this.view.getBlockReader(x >> 4, z >> 4);

        if (chunk == null) {
            return true;
        }

        final BlockPos.Mutable mpos = this.mpos;
        mpos.setPos(x, y, z);

        final BlockState state = chunk.getBlockState(mpos);

        if (canInteractWithBlock(state, edgesHit)) {
            VoxelShape collisionShape = state.getCollisionShape(this.view, mpos, this.context);

            if (collisionShape != VoxelShapes.empty()) {
                this.collidedShape = getCollidedShape(this.box, this.shape, collisionShape, x, y, z);
            }
        }

        return true;

    }

    /**
     * @return The shape collided with during the last step, otherwise null
     */
    public VoxelShape getCollidedShape() {
        return this.collidedShape;
    }

    /**
     * Returns an iterator which will include every block position that can contain a collision shape which can interact
     * with the {@param box}.
     */
    private static CubeCoordinateIterator createVolumeIteratorForCollision(AxisAlignedBB box) {
        int minX = MathHelper.floor(box.minX - EPSILON) - 1;
        int maxX = MathHelper.floor(box.maxX + EPSILON) + 1;
        int minY = MathHelper.floor(box.minY - EPSILON) - 1;
        int maxY = MathHelper.floor(box.maxY + EPSILON) + 1;
        int minZ = MathHelper.floor(box.minZ - EPSILON) - 1;
        int maxZ = MathHelper.floor(box.maxZ + EPSILON) + 1;

        return new CubeCoordinateIterator(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * This is an artifact from vanilla which is used to avoid testing shapes in the extended portion of a volume
     * unless they are a shape which exceeds their voxel. Pistons must be special-cased here.
     *
     * @return True if the shape can be interacted with at the given edge boundary
     */
    private static boolean canInteractWithBlock(BlockState state, int edgesHit) {
        return (edgesHit != 1 || state.isCollisionShapeLargerThanFullBlock()) && (edgesHit != 2 || state.getBlock() == Blocks.MOVING_PISTON);
    }

    /**
     * Checks if the {@param entityShape} or {@param entityBox} intersects the given {@param shape} which is translated
     * to the given position. This is a very specialized implementation which tries to avoid going through VoxelShape
     * for full-cube shapes.
     *
     * @return A {@link VoxelShape} which contains the shape representing that which was collided with, otherwise null
     */
    private static VoxelShape getCollidedShape(AxisAlignedBB entityBox, VoxelShape entityShape, VoxelShape shape, int x, int y, int z) {
        if (shape instanceof VoxelShapeCaster) {
            if (((VoxelShapeCaster) shape).intersects(entityBox, x, y, z)) {
                return shape.withOffset(x, y, z);
            } else {
                return null;
            }
        }

        shape = shape.withOffset(x, y, z);

        if (VoxelShapes.compare(shape, entityShape, IBooleanFunction.AND)) {
            return shape;
        }

        return null;
    }
}
