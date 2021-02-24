package me.jellysquid.mods.lithium.mixin.alloc.chunk_ticking;

import com.google.common.collect.Iterators;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;

@Mixin(ServerChunkProvider.class)
public class ServerChunkManagerMixin {
    private final ArrayList<ChunkHolder> cachedChunkList = new ArrayList<>();

    @Redirect(
            method = "tickChunks",
            at = @At(
                    remap = false,
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Lists;newArrayList(Ljava/lang/Iterable;)Ljava/util/ArrayList;"
            )
    )
    private ArrayList<ChunkHolder> redirectChunksListClone(Iterable<? extends ChunkHolder> elements) {
        ArrayList<ChunkHolder> list = this.cachedChunkList;
        list.clear(); // Ensure the list is empty before re-using it

        Iterators.addAll(list, elements.iterator());

        return list;
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("HEAD"))
    private void preTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        // Ensure references aren't leaked through this list
        this.cachedChunkList.clear();
    }
}
