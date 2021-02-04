package me.jellysquid.mods.lithium.common.world;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.entity.EntityClassGroup;
import me.jellysquid.mods.lithium.common.world.chunk.ClassGroupFilterableList;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.util.EntityPredicates.NOT_SPECTATING;

public class WorldHelper {
    public interface MixinLoadTest {
    }

    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !MixinLoadTest.class.isAssignableFrom(ClassInheritanceMultiMap.class);


    /**
     * Partial [VanillaCopy] Classes overriding Entity.getHardCollisionBox(Entity other) or Entity.getCollisionBox()
     * The returned entity list is only used to call getCollisionBox and getHardCollisionBox. As most entities return null
     * for both of these methods, getting those is not necessary. This is why we only get entities when they overwrite
     * getCollisionBox
     *
     * @param entityView      the world
     * @param box             the box the entities have to collide with
     * @param collidingEntity the entity that is searching for the colliding entities
     * @return list of entities with collision boxes
     */
    public static List<Entity> getEntitiesWithCollisionBoxForEntity(IEntityReader entityView, AxisAlignedBB box, Entity collidingEntity) {
        if (CUSTOM_TYPE_FILTERABLE_LIST_DISABLED || collidingEntity != null && EntityClassGroup.MINECART_BOAT_LIKE_COLLISION.contains(collidingEntity.getClass()) || !(entityView instanceof World)) {
            //use vanilla code when method_30949 (previously getHardCollisionBox(Entity other)) is overwritten, as every entity could be relevant as argument of getHardCollisionBox
            return entityView.getEntitiesWithinAABBExcludingEntity(collidingEntity, box);
        } else {
            //only get entities that overwrite method_30948 (previously getCollisionBox)
            return getEntitiesOfClassGroup((World) entityView, collidingEntity, EntityClassGroup.BOAT_SHULKER_LIKE_COLLISION, box, NOT_SPECTATING);
        }
    }

    /**
     * Method that allows getting entities of a class group.
     * [VanillaCopy] but custom combination of: get class filtered entities together with excluding one entity
     */
    public static List<Entity> getEntitiesOfClassGroup(World world, Entity excluded, EntityClassGroup type, AxisAlignedBB box, Predicate<Entity> predicate) {
        world.getProfiler().func_230035_c_("getEntities");

        int minChunkX = MathHelper.floor((box.minX - 2.0D) / 16.0D);
        int maxChunkX = MathHelper.ceil((box.maxX + 2.0D) / 16.0D);
        int minChunkZ = MathHelper.floor((box.minZ - 2.0D) / 16.0D);
        int maxChunkZ = MathHelper.ceil((box.maxZ + 2.0D) / 16.0D);

        List<Entity> entities = Lists.newArrayList();
        AbstractChunkProvider chunkManager = world.getChunkProvider();

        for (int chunkX = minChunkX; chunkX < maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++) {
                Chunk chunk = chunkManager.getChunk(chunkX, chunkZ, false);

                if (chunk != null) {
                    WorldHelper.getEntitiesOfClassGroup(chunk, excluded, type, box, entities, predicate);
                }
            }
        }

        return entities;
    }

    /**
     * Method that allows getting entities of a class group.
     * [VanillaCopy] but custom combination of: get class filtered entities together with excluding one entity
     */
    public static void getEntitiesOfClassGroup(Chunk worldChunk, Entity excluded, EntityClassGroup type, AxisAlignedBB box, List<Entity> out, Predicate<Entity> predicate) {
        ClassInheritanceMultiMap<Entity>[] entitySections = worldChunk.getEntityLists();
        int minSectionY = MathHelper.floor((box.minY - 2.0D) / 16.0D);
        int maxSectionY = MathHelper.floor((box.maxY + 2.0D) / 16.0D);

        minSectionY = MathHelper.clamp(minSectionY, 0, entitySections.length - 1);
        maxSectionY = MathHelper.clamp(maxSectionY, 0, entitySections.length - 1);

        for (int sectionY = minSectionY; sectionY <= maxSectionY; ++sectionY) {
            //noinspection rawtypes
            for (Object entity : ((ClassGroupFilterableList) entitySections[sectionY]).getAllOfGroupType(type)) {
                if (entity != excluded && ((Entity) entity).getBoundingBox().intersects(box) && (predicate == null || predicate.test((Entity) entity))) {
                    out.add((Entity) entity);
                }
            }
        }
    }


    /**
     * [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    public static List<Entity> getEntitiesOfClass(World world, Entity except, Class<? extends Entity> entityClass, AxisAlignedBB box) {
        world.getProfiler().func_230035_c_("getEntities");

        int minChunkX = MathHelper.floor((box.minX - 2.0D) / 16.0D);
        int maxChunkX = MathHelper.ceil((box.maxX + 2.0D) / 16.0D);
        int minChunkZ = MathHelper.floor((box.minZ - 2.0D) / 16.0D);
        int maxChunkZ = MathHelper.ceil((box.maxZ + 2.0D) / 16.0D);

        List<Entity> entities = Lists.newArrayList();
        AbstractChunkProvider chunkManager = world.getChunkProvider();

        for (int chunkX = minChunkX; chunkX < maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; ++chunkZ) {
                Chunk chunk = chunkManager.getChunk(chunkX, chunkZ, false);

                if (chunk != null) {
                    WorldHelper.getEntitiesOfClass(chunk, except, entityClass, box, entities);
                }
            }
        }

        return entities;
    }

    /**
     * [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    private static void getEntitiesOfClass(Chunk worldChunk, Entity excluded, Class<? extends Entity> entityClass, AxisAlignedBB box, List<Entity> out) {
        ClassInheritanceMultiMap<Entity>[] entitySections = worldChunk.getEntityLists();
        int minChunkY = MathHelper.floor((box.minY - 2.0D) / 16.0D);
        int maxChunkY = MathHelper.floor((box.maxY + 2.0D) / 16.0D);
        minChunkY = MathHelper.clamp(minChunkY, 0, entitySections.length - 1);
        maxChunkY = MathHelper.clamp(maxChunkY, 0, entitySections.length - 1);

        for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
            for (Entity entity : entitySections[chunkY].getByClass(entityClass)) {
                if (entity != excluded && entity.getBoundingBox().intersects(box)) {
                    out.add(entity);
                }
            }
        }
    }

    public static boolean areNeighborsWithinSameChunk(BlockPos pos) {
        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15;
    }

    public static boolean areAllNeighborsOutOfBounds(BlockPos pos) {
        return pos.getY() < -1 || pos.getY() > 256;
    }
}
