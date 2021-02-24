package me.jellysquid.mods.lithium.mixin.chunk.serialization;

import me.jellysquid.mods.lithium.common.world.chunk.CompactingPackedIntegerArray;
import me.jellysquid.mods.lithium.common.world.chunk.LithiumHashPalette;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.BitArray;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.palette.IPalette;
import net.minecraft.util.palette.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

/**
 * Makes a number of patches to {@link PalettedContainer} to speed up integer array compaction. While I/O operations
 * in Minecraft 1.15+ are handled off-thread, NBT serialization is not and happens on the main server thread.
 */
@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> {
    private static final ThreadLocal<short[]> cachedCompactionArrays = ThreadLocal.withInitial(() -> new short[4096]);
    private static final long[] EMPTY_PALETTE_DATA = new long[(4 * 4096) / 64];

    @Shadow
    public abstract void lock();

    @Shadow
    public abstract void unlock();

    @Shadow
    protected abstract T get(int index);

    @Shadow
    @Final
    private T defaultState;

    @Shadow
    @Final
    private ObjectIntIdentityMap<T> registry;

    @Shadow
    private int bits;

    @Shadow
    @Final
    private Function<CompoundNBT, T> deserializer;

    @Shadow
    @Final
    private Function<T, CompoundNBT> serializer;

    @Shadow
    protected BitArray storage;

    @Shadow
    private IPalette<T> palette;

    /**
     * This patch incorporates a number of changes to significantly reduce the time needed to serialize.
     * - If a palette only contains one entry, do not attempt to repack it
     * - The packed integer array is iterated over using a specialized consumer instead of a naive for-loop.
     * - A temporary fixed array is used to cache palette lookups and remaps while compacting a data array.
     * - If the palette didn't change after compaction, avoid the step of re-packing the integer array and instead do
     * a simple memory copy.
     *
     * @reason Optimize serialization
     * @author JellySquid
     */
    @Overwrite
    public void writeChunkPalette(CompoundNBT rootTag, String paletteKey, String dataKey) {
        this.lock();

        // The palette that will be serialized
        LithiumHashPalette<T> palette = null;
        long[] dataArray = null;

        if (this.palette instanceof LithiumHashPalette) {
            palette = ((LithiumHashPalette<T>) this.palette);

            // The palette only contains the default block, so don't re-pack
            if (palette.getSize() == 1 && palette.get(0) == this.defaultState) {
                dataArray = EMPTY_PALETTE_DATA;
            }
        }

        // If we aren't going to use an empty data array, start a compaction
        if (dataArray == null) {
            LithiumHashPalette<T> compactedPalette = new LithiumHashPalette<>(this.registry, this.bits, null, this.deserializer, this.serializer);

            short[] array = cachedCompactionArrays.get();
            ((CompactingPackedIntegerArray) this.storage).compact(this.palette, compactedPalette, array);

            // If the palette didn't change during compaction, do a simple copy of the data array
            if (palette != null && palette.getSize() == compactedPalette.getSize()) {
                dataArray = this.storage.getBackingLongArray().clone();
            } else {
                // Re-pack the integer array as the palette has changed size
                int size = Math.max(4, MathHelper.log2DeBruijn(compactedPalette.getSize()));
                BitArray copy = new BitArray(size, 4096);

                for (int i = 0; i < array.length; ++i) {
                    copy.setAt(i, array[i]);
                }

                // We don't need to clone the data array as we are the sole owner of it
                dataArray = copy.getBackingLongArray();
                palette = compactedPalette;
            }
        }

        ListNBT paletteTag = new ListNBT();
        palette.toTag(paletteTag);

        rootTag.put(paletteKey, paletteTag);
        rootTag.putLongArray(dataKey, dataArray);

        this.unlock();
    }

    /**
     * If we know the palette will contain a fixed number of elements, we can make a significant optimization by counting
     * blocks with a simple array instead of a integer map. Since palettes make no guarantee that they are bounded,
     * we have to try and determine for each implementation type how many elements there are.
     *
     * @author JellySquid
     */
    @Inject(method = "count", at = @At("HEAD"), cancellable = true)
    public void count(PalettedContainer.ICountConsumer<T> consumer, CallbackInfo ci) {
        int len = (1 << this.bits);

        // Do not allocate huge arrays if we're using a large palette
        if (len > 4096) {
            return;
        }

        short[] counts = new short[len];

        this.storage.getAll(i -> counts[i]++);

        for (int i = 0; i < counts.length; i++) {
            T obj = this.palette.get(i);

            if (obj != null) {
                consumer.accept(obj, counts[i]);
            }
        }

        ci.cancel();
    }
}
