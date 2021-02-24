package me.jellysquid.mods.lithium.mixin.gen.cached_generator_settings;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;

// TODO(lithium-forge): investigate whether this patch can just use a 'RETURN' injection point
@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {
    // lithium-forge: add shadow for field
    @Shadow
    @Final
    protected Supplier<DimensionSettings> field_236080_h_;

    // lithium-forge: use default value of -1, so we know when to grab value from registry.
    private int cachedSeaLevel = -1;

    /**
     * Use cached sea level instead of retrieving from the registry every time.
     * This method is called for every block in the chunk so this will save a lot of registry lookups.
     *
     * @author SuperCoder79
     */
    @Overwrite
    public int getSeaLevel() {
        // lithium-forge: pull value from registry and cache
        if (this.cachedSeaLevel == -1) {
            this.cachedSeaLevel = this.field_236080_h_.get().func_236119_g_();
        }
        // lithium-forge: end
        return this.cachedSeaLevel;
    }

    /* lithium-forge: Mixin doesn't 'INVOKE' injections on constructors, we cache this value
     * on demand (above) instead.
    /**
     * Initialize the cache early in the ctor to avoid potential future problems with uninitialized usages
     */
    /*
    @Inject(
            method = "<init>(Lnet/minecraft/world/biome/provider/BiomeProvider;Lnet/minecraft/world/biome/provider/BiomeProvider;JLjava/util/function/Supplier;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/DimensionSettings;getNoise()Lnet/minecraft/world/gen/settings/NoiseSettings;",
                    shift = At.Shift.BEFORE
            )
    )
    private void hookConstructor(BiomeProvider populationSource, BiomeProvider biomeSource, long seed, Supplier<DimensionSettings> settings, CallbackInfo ci) {
        this.cachedSeaLevel = settings.get().func_236119_g_();
    }
     */
}
