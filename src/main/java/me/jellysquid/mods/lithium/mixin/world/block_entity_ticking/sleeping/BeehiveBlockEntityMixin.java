package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.util.collections.ListeningList;
import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import me.jellysquid.mods.lithium.common.world.blockentity.SleepingBlockEntity;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BeehiveTileEntity.class)
public class BeehiveBlockEntityMixin extends TileEntity implements SleepingBlockEntity {

    @Mutable
    @Shadow
    @Final
    private List<?> bees;

    @Unique
    private boolean isTicking;
    @Unique
    private boolean doInit;

    public BeehiveBlockEntityMixin(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public boolean canTickOnSide(boolean isClient) {
        return !isClient;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void createInhabitantListener(CallbackInfo ci) {
        this.doInit = true;
        this.isTicking = true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void firstTick(CallbackInfo ci) {
        if (this.doInit) {
            this.bees = new ListeningList<>(this.bees, this::checkSleepState);
            this.doInit = false;
            this.checkSleepState();
        }
    }

    private void checkSleepState() {
        if (this.world != null && !this.world.isRemote) {
            if ((this.bees.size() == 0) == this.isTicking) {
                this.isTicking = !this.isTicking;
                ((BlockEntitySleepTracker) this.world).setAwake(this, this.isTicking);
            }
        }

    }
}
