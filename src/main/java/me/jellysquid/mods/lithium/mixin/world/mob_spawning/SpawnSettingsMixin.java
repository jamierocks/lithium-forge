package me.jellysquid.mods.lithium.mixin.world.mob_spawning;

import com.google.common.collect.Maps;
import me.jellysquid.mods.lithium.common.util.collections.HashedReferenceList;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.MobSpawnInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(MobSpawnInfo.class)
public class SpawnSettingsMixin {
    @Mutable
    @Shadow
    @Final
    private Map<EntityClassification, List<MobSpawnInfo.Spawners>> spawners;

    /**
     * Re-initialize the spawn category lists with a much faster backing collection type for enum keys. This provides
     * a modest speed-up for mob spawning as {@link MobSpawnInfo#getSpawners(EntityClassification)} is a rather hot method.
     * <p>
     * Additionally, the list containing each spawn entry is modified to include a hash table for lookups, making them
     * O(1) instead of O(n) and providing another boost when lists get large. Since a simple wrapper type is used, this
     * should provide good compatibility with other mods which modify spawn entries.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(float creatureSpawnProbability, Map<EntityClassification, List<MobSpawnInfo.Spawners>> spawners, Map<EntityType<?>, MobSpawnInfo.SpawnCosts> spawnCosts, boolean playerSpawnFriendly, CallbackInfo ci) {
        Map<EntityClassification, List<MobSpawnInfo.Spawners>> spawns = Maps.newEnumMap(EntityClassification.class);

        for (Map.Entry<EntityClassification, List<MobSpawnInfo.Spawners>> entry : this.spawners.entrySet()) {
            spawns.put(entry.getKey(), new HashedReferenceList<>(entry.getValue()));
        }

        this.spawners = spawns;
    }
}
