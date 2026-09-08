package me.contaria.seedqueue.mixin.server.synchronization.block.bounding_box;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(Block.class)
public abstract class BlockMixin {
    /**
     * The initial bounding box used to initialize thread-local bounding boxes.
     */
    @Unique
    private double[] initialBoundingBox;
    /**
     * The bounding box used by the client and the currently active server.
     */
    @Unique
    private double[] mainBoundingBox;
    /**
     * The thread-local bounding boxes used by queued servers.
     * If this is null, initialBoundingBox is used instead.
     */
    @Unique
    protected volatile ThreadLocal<double[]> threadedBoundingBox;

    /**
     * @author contaria
     * @reason Fully replace bounding box fields to replicate the behaviour where client and server share
     *         the same fields in an unsafe manner without allowing servers in queue to affect them.
     */
    @Overwrite
    public final void setBoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (this.initialBoundingBox == null) {
            this.initialBoundingBox = new double[]{minX, minY, minZ, maxX, maxY, maxZ};
            this.mainBoundingBox = new double[]{minX, minY, minZ, maxX, maxY, maxZ};
            return;
        }
        this.setThreadedBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Redirect(
            method = "getMinX",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMinX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinX(Block block) {
        return this.getThreadedBoundingBox()[0];
    }

    @Redirect(
            method = "getMinY",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMinY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinY(Block block) {
        return this.getThreadedBoundingBox()[1];
    }

    @Redirect(
            method = "getMinZ",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMinZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinZ(Block block) {
        return this.getThreadedBoundingBox()[2];
    }

    @Redirect(
            method = "getMaxX",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMaxX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxX(Block block) {
        return this.getThreadedBoundingBox()[3];
    }

    @Redirect(
            method = "getMaxY",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMaxY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxY(Block block) {
        return this.getThreadedBoundingBox()[4];
    }

    @Redirect(
            method = "getMaxZ",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMaxZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxZ(Block block) {
        return this.getThreadedBoundingBox()[5];
    }

    @Inject(
            method = {
                    "isSideInvisible",
                    "getSelectionBox",
                    "getCollisionBox",
                    "rayTrace",
                    "isVecWithinXYBounds",
                    "isVecWithinXZBounds",
                    "isVecWithinYZBounds"
            },
            at = @At("HEAD")
    )
    private void getThreadedBoundingBox(CallbackInfoReturnable<Boolean> cir, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        boundingBox.set(this.getThreadedBoundingBox());
    }

    @Redirect(
            method = {
                    "isSideInvisible",
                    "getSelectionBox",
                    "getCollisionBox",
                    "rayTrace",
                    "isVecWithinXYBounds",
                    "isVecWithinXZBounds"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMinX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinX(Block block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[0];
    }

    @Redirect(
            method = {
                    "isSideInvisible",
                    "getSelectionBox",
                    "getCollisionBox",
                    "rayTrace",
                    "isVecWithinXYBounds",
                    "isVecWithinYZBounds"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMinY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinY(Block block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[1];
    }

    @Redirect(
            method = {
                    "isSideInvisible",
                    "getSelectionBox",
                    "getCollisionBox",
                    "rayTrace",
                    "isVecWithinXZBounds",
                    "isVecWithinYZBounds"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMinZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMinZ(Block block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[2];
    }

    @Redirect(
            method = {
                    "isSideInvisible",
                    "getSelectionBox",
                    "getCollisionBox",
                    "rayTrace",
                    "isVecWithinXYBounds",
                    "isVecWithinXZBounds"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMaxX:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxX(Block block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[3];
    }

    @Redirect(
            method = {
                    "isSideInvisible",
                    "getSelectionBox",
                    "getCollisionBox",
                    "rayTrace",
                    "isVecWithinXYBounds",
                    "isVecWithinYZBounds"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMaxY:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxY(Block block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[4];
    }

    @Redirect(
            method = {
                    "isSideInvisible",
                    "getSelectionBox",
                    "getCollisionBox",
                    "rayTrace",
                    "isVecWithinXZBounds",
                    "isVecWithinYZBounds"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/block/Block;boundingBoxMaxZ:D",
                    opcode = Opcodes.GETFIELD
            )
    )
    private double getThreadedMaxZ(Block block, @Share("boundingBox") LocalRef<double[]> boundingBox) {
        return boundingBox.get()[5];
    }

    @Unique
    protected double[] getThreadedBoundingBox() {
        if (this.threadedBoundingBox == null) {
            return this.initialBoundingBox;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        MinecraftServer server = client.getServer();
        if (client.isOnThread() || (server != null && server.isOnThread())) {
            return this.mainBoundingBox;
        }
        return this.threadedBoundingBox.get();
    }

    @Unique
    protected double[] getOrCreateThreadedBoundingBox() {
        if (this.threadedBoundingBox == null) {
            synchronized (this) {
                if (this.threadedBoundingBox == null) {
                    this.threadedBoundingBox = ThreadLocal.withInitial(() -> Arrays.copyOf(this.initialBoundingBox, 6));
                }
            }
        }
        return this.getThreadedBoundingBox();
    }

    @Unique
    protected final void setThreadedBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        double[] boundingBox = this.getOrCreateThreadedBoundingBox();
        boundingBox[0] = minX;
        boundingBox[1] = minY;
        boundingBox[2] = minZ;
        boundingBox[3] = maxX;
        boundingBox[4] = maxY;
        boundingBox[5] = maxZ;
    }
}
