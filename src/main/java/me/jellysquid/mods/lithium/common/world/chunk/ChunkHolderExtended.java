package me.jellysquid.mods.lithium.common.world.chunk;

import com.mojang.datafixers.util.Either;
import java.util.concurrent.CompletableFuture;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ChunkHolder;

public interface ChunkHolderExtended {
    /**
     * @return The existing future for the status at ordiinal {@param index} or null if none exists
     */
    CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> getFutureByStatus(int index);

    /**
     * Updates the future for the status at ordinal {@param index}.
     */
    void setFutureForStatus(int index, CompletableFuture<Either<IChunk, ChunkHolder.IChunkLoadingError>> future);

    /**
     * Updates the last accessed timestamp for this chunk. This is used to determine if a ticket was recently
     * created for it.
     *
     * @param time The current time
     * @return True if the chunk needs a new ticket to be created in order to retain it, otherwise false
     */
    boolean updateLastAccessTime(long time);
}
