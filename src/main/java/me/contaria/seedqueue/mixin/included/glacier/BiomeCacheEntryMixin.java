package me.contaria.seedqueue.mixin.included.glacier;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.LayeredBiomeSource;
import net.minecraft.world.biome.BiomeCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BiomeCache.Entry.class)
public abstract class BiomeCacheEntryMixin {
    @Shadow
    public float[] field_7227;

    @Definition(id = "field_7227", field = "Lnet/minecraft/world/biome/BiomeCache$Entry;field_7227:[F")
    @Expression("this.field_7227 = new float[@(256)]")
    @ModifyExpressionValue(
            method = "<init>",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private int emptyDownFalls(int length) {
        return 0;
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/LayeredBiomeSource;method_3856([FIIII)[F"
            )
    )
    private float[] doNotPopulateDownFalls(LayeredBiomeSource biomeSource, float[] downFalls, int x, int z, int width, int height) {
        return this.field_7227 = null;
    }
}
