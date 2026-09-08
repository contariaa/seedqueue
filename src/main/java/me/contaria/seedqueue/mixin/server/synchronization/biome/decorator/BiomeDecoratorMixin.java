package me.contaria.seedqueue.mixin.server.synchronization.biome.decorator;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StoneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.CustomizedWorldProperties;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BiomeDecorator.class)
public abstract class BiomeDecoratorMixin {
    @Unique
    protected final ThreadLocal<World> threadedWorld = new ThreadLocal<>();
    @Unique
    protected final ThreadLocal<Random> threadedRandom = new ThreadLocal<>();
    @Unique
    protected final ThreadLocal<BlockPos> threadedStartPos = new ThreadLocal<>();
    @Unique
    protected final ThreadLocal<CustomizedWorldProperties> threadedWorldProperties = new ThreadLocal<>();

    @Redirect(
            method = "decorate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;world:Lnet/minecraft/world/World;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedWorld(BiomeDecorator decorator, World world) {
        this.threadedWorld.set(world);
    }

    @Redirect(
            method = "decorate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;random:Ljava/util/Random;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedRandom(BiomeDecorator decorator, Random random) {
        this.threadedRandom.set(random);
    }

    @Redirect(
            method = "decorate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;startPos:Lnet/minecraft/util/math/BlockPos;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedStartPos(BiomeDecorator decorator, BlockPos startPos) {
        this.threadedStartPos.set(startPos);
    }

    @Redirect(
            method = "decorate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;worldProperties:Lnet/minecraft/world/gen/CustomizedWorldProperties;",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void setThreadedWorldProperties(BiomeDecorator decorator, CustomizedWorldProperties worldProperties) {
        this.threadedWorldProperties.set(worldProperties);
    }

    @WrapOperation(
            method = "decorate",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/block/BlockState;I)Lnet/minecraft/world/gen/feature/OreFeature;"
            )
    )
    private OreFeature noOreFeatureInstances(BlockState blockState, int i, Operation<OreFeature> original) {
        return null;
    }

    @Inject(
            method = "decorate",
            at = @At("TAIL")
    )
    private void removeThreadLocals(CallbackInfo ci) {
        this.threadedWorld.remove();
        this.threadedRandom.remove();
        this.threadedStartPos.remove();
        this.threadedWorldProperties.remove();
    }

    @Redirect(
            method = {
                    "decorate",
                    "generate",
                    "generateOre",
                    "method_3850"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;world:Lnet/minecraft/world/World;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private World getThreadedWorld(BiomeDecorator decorator) {
        return this.threadedWorld.get();
    }

    @Redirect(
            method = {
                    "generate",
                    "generateOre",
                    "method_3850"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;random:Ljava/util/Random;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Random getThreadedRandom(BiomeDecorator decorator) {
        return this.threadedRandom.get();
    }

    @Redirect(
            method = {
                    "generate",
                    "generateOre",
                    "method_3850"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;startPos:Lnet/minecraft/util/math/BlockPos;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private BlockPos getThreadedStartPos(BiomeDecorator decorator) {
        return this.threadedStartPos.get();
    }

    @Redirect(
            method = {
                    "decorate",
                    "generateOres"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;worldProperties:Lnet/minecraft/world/gen/CustomizedWorldProperties;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private CustomizedWorldProperties getThreadedWorldProperties(BiomeDecorator decorator) {
        return this.threadedWorldProperties.get();
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;dirtFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createDirtFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.DIRT.getDefaultState(), this.threadedWorldProperties.get().dirtSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;gravelFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createGravelFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.GRAVEL.getDefaultState(), this.threadedWorldProperties.get().gravelSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;dioriteFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createDioriteFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.STONE.getDefaultState().with(StoneBlock.VARIANT, StoneBlock.StoneType.DIORITE), this.threadedWorldProperties.get().dioriteSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;graniteFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createGraniteFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.STONE.getDefaultState().with(StoneBlock.VARIANT, StoneBlock.StoneType.GRANITE), this.threadedWorldProperties.get().graniteSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;andesiteFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createAndesiteFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.STONE.getDefaultState().with(StoneBlock.VARIANT, StoneBlock.StoneType.ANDESITE), this.threadedWorldProperties.get().andesiteSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;coalOreFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createCoalFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.COAL_ORE.getDefaultState(), this.threadedWorldProperties.get().coalSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;ironOreFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createIronFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.IRON_ORE.getDefaultState(), this.threadedWorldProperties.get().ironSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;goldOreFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createGoldFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.GOLD_ORE.getDefaultState(), this.threadedWorldProperties.get().goldSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;redstoneOreFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createRedstoneFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.REDSTONE_ORE.getDefaultState(), this.threadedWorldProperties.get().redstoneSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;diamondOreFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createDiamondFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.DIAMOND_ORE.getDefaultState(), this.threadedWorldProperties.get().diamondSize);
    }

    @Redirect(
            method = "generateOres",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/biome/BiomeDecorator;lapisOreFeature:Lnet/minecraft/world/gen/feature/Feature;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private Feature createLapisFeature(BiomeDecorator decorator) {
        return new OreFeature(Blocks.LAPIS_LAZULI_ORE.getDefaultState(), this.threadedWorldProperties.get().lapisSize);
    }
}
