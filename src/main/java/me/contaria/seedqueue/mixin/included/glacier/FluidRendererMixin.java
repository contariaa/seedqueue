package me.contaria.seedqueue.mixin.included.glacier;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void createMutableBlockPos(CallbackInfoReturnable<Boolean> cir, @Share("mutable") LocalRef<BlockPos.Mutable> mutable) {
        mutable.set(new BlockPos.Mutable());
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;up()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos useMutableBlockPosUp(BlockPos pos, @Share("mutable") LocalRef<BlockPos.Mutable> mutable) {
        return mutable.get().setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;down()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos useMutableBlockPosDown(BlockPos pos, @Share("mutable") LocalRef<BlockPos.Mutable> mutable) {
        return mutable.get().setPosition(pos.getX(), pos.getY() - 1, pos.getZ());
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;north()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos useMutableBlockPosNorth(BlockPos pos, @Share("mutable") LocalRef<BlockPos.Mutable> mutable) {
        return mutable.get().setPosition(pos.getX(), pos.getY(), pos.getZ() - 1);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;south()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos useMutableBlockPosSouth(BlockPos pos, @Share("mutable") LocalRef<BlockPos.Mutable> mutable) {
        return mutable.get().setPosition(pos.getX(), pos.getY(), pos.getZ() + 1);
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;west()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos useMutableBlockPosWest(BlockPos pos, @Share("mutable") LocalRef<BlockPos.Mutable> mutable) {
        return mutable.get().setPosition(pos.getX() - 1, pos.getY(), pos.getZ());
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;east()Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos useMutableBlockPosEast(BlockPos pos, @Share("mutable") LocalRef<BlockPos.Mutable> mutable) {
        return mutable.get().setPosition(pos.getX() + 1, pos.getY(), pos.getZ());
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;add(III)Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private BlockPos useMutableBlockPosAdd(BlockPos pos, int x, int y, int z, @Share("mutable") LocalRef<BlockPos.Mutable> mutable) {
        return mutable.get().setPosition(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
    }
}
