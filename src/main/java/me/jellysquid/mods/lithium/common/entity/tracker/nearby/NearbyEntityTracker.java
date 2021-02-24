package me.jellysquid.mods.lithium.common.entity.tracker.nearby;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import java.util.Set;

/**
 * Maintains a collection of all entities within the range of this listener. This allows AI goals to quickly
 * assess nearby entities which match the provided class.
 */
public class NearbyEntityTracker<T extends LivingEntity> implements NearbyEntityListener {
    private final Class<T> clazz;
    private final LivingEntity self;

    private final int rangeC;
    private final float rangeSq;

    private final Set<T> nearby = new ReferenceOpenHashSet<>();

    public NearbyEntityTracker(Class<T> clazz, LivingEntity self, float range) {
        this.clazz = clazz;
        this.self = self;
        this.rangeSq = range * range;
        this.rangeC = Math.max((MathHelper.ceil(range) + 15) >> 4, 1);
    }

    @Override
    public int getChunkRange() {
        return this.rangeC;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEntityEnteredRange(LivingEntity entity) {
        if (!this.clazz.isInstance(entity)) {
            return;
        }

        this.nearby.add((T) entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEntityLeftRange(LivingEntity entity) {
        if (this.nearby.isEmpty() || !this.clazz.isInstance(entity)) {
            return;
        }

        this.nearby.remove((T) entity);
    }

    /**
     * Gets the closest T (extends LivingEntity) to the center of this tracker that also intersects with the given box and meets the
     * requirements of the targetPredicate.
     * The result may be different from vanilla if there are multiple closest entities.
     *
     * @param box             the box the entities have to intersect
     * @param targetPredicate predicate the entity has to meet
     * @return the closest Entity that meets all requirements (distance, box intersection, predicate, type T)
     */
    public T getClosestEntity(AxisAlignedBB box, EntityPredicate targetPredicate) {
        double x = this.self.getPosX();
        double y = this.self.getPosY();
        double z = this.self.getPosZ();

        T nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;

        for (T entity : this.nearby) {
            double distance = entity.getDistanceSq(x, y, z);

            if (distance < nearestDistance && (box == null || box.intersects(entity.getBoundingBox())) && targetPredicate.canTarget(this.self, entity)) {
                nearest = entity;
                nearestDistance = distance;
            }
        }

        if (nearestDistance <= this.rangeSq) {
            return nearest;
        }

        return null;
    }

    @Override
    public String toString() {
        return super.toString() + " for entity class: " + this.clazz.getName() + ", in rangeSq: " + this.rangeSq + ", around entity: " + this.self.toString() + " with NBT: " + this.self.writeWithoutTypeId(new CompoundNBT());
    }
}
