package me.jellysquid.mods.lithium.mixin.entity.replace_entitytype_predicates;

import me.jellysquid.mods.lithium.common.world.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(LlamaFollowCaravanGoal.class)
public class FormCaravanGoalMixin {
    @Redirect(
            method = "shouldExecute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private List<Entity> getLlamasForCaravan(World world, Entity excluded, AxisAlignedBB box, Predicate<? super Entity> predicate) {
        return WorldHelper.getEntitiesOfClass(world, excluded, LlamaEntity.class, box);
    }
}
