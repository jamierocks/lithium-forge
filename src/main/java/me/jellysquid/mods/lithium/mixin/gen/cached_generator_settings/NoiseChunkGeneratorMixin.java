package me.jellysquid.mods.lithium.mixin.gen.cached_generator_settings;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.NoiseChunkGenerator;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {
    private int cachedSeaLevel;

    /**
     * Use cached sea level instead of retrieving from the registry every time.
     * This method is called for every block in the chunk so this will save a lot of registry lookups.
     *
     * @author SuperCoder79
     */
    @Overwrite
    public int getSeaLevel() {
        return this.cachedSeaLevel;
    }

    /**
     * Initialize the cache early in the ctor to avoid potential future problems with uninitialized usages
     */
    @Inject(
            method = "<init>(Lnet/minecraft/world/biome/provider/BiomeProvider;Lnet/minecraft/world/biome/provider/BiomeProvider;JLjava/util/function/Supplier;)V",
            at = @At(
                    "RETURN"
                    // lithium-forge: Mixin doesn't support injecting in arbitrary places in constructors
                    // this is functionality added by a fork.
                    // It seems safe to inject at "RETURN", so that's what I've gone with.
                    /*
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/DimensionSettings;getNoise()Lnet/minecraft/world/gen/settings/NoiseSettings;",
                    shift = At.Shift.BEFORE
                     */
            )
    )
    private void hookConstructor(BiomeProvider populationSource, BiomeProvider biomeSource, long seed, Supplier<DimensionSettings> settings, CallbackInfo ci) {
        this.cachedSeaLevel = settings.get().func_236119_g_();
    }
}
