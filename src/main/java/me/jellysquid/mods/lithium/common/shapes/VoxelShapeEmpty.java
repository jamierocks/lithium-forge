package me.jellysquid.mods.lithium.common.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapePart;

/**
 * An efficient implementation of {@link VoxelShape} for a shape with no vertices. Vanilla normally represents this
 * case with an empty {@link net.minecraft.util.math.shapes.VoxelShapeCube}, but since there is no data the return values
 * here will always be constant. This allows a lot of unnecessary code to be eliminated that would otherwise try to
 * iterate over sets of empty voxels/vertices.
 */
public class VoxelShapeEmpty extends VoxelShape implements VoxelShapeCaster {
    private static final DoubleList EMPTY_LIST = DoubleArrayList.wrap(new double[]{0.0D});

    public VoxelShapeEmpty(VoxelShapePart voxels) {
        super(voxels);
    }

    @Override
    public DoubleList getValues(Direction.Axis axis) {
        return EMPTY_LIST;
    }

    @Override
    protected boolean contains(double x, double y, double z) {
        return false;
    }

    @Override
    public double getStart(Direction.Axis axis) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getEnd(Direction.Axis axis) {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean intersects(AxisAlignedBB box, double x, double y, double z) {
        return false;
    }
}
