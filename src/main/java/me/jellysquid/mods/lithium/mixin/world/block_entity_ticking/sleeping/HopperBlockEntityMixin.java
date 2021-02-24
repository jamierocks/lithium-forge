package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.SleepingBlockEntity;
import net.minecraft.tileentity.HopperTileEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HopperTileEntity.class)
public class HopperBlockEntityMixin implements SleepingBlockEntity {
    @Override
    public boolean canTickOnSide(boolean isClient) {
        return !isClient;
    }
}
