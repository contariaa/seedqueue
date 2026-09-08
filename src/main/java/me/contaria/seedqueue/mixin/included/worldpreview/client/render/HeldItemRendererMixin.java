package me.contaria.seedqueue.mixin.included.worldpreview.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @ModifyExpressionValue(
            method = {
                    "renderMap",
                    "applyEatOrDrinkTransformation",
                    "applyBowTransformation",
                    "renderArmHoldingItem"
            },
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/item/HeldItemRenderer;mainHand:Lnet/minecraft/item/ItemStack;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ItemStack modifyMainHand(ItemStack mainHand) {
        if (WorldPreview.renderingPreview) {
            return WorldPreview.properties.player.getMainHandStack();
        }
        return mainHand;
    }

    @ModifyExpressionValue(
            method = "renderArmHoldingItem",
            at = {
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;equipProgress:F",
                            opcode = Opcodes.GETFIELD
                    ),
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;lastEquipProgress:F",
                            opcode = Opcodes.GETFIELD
                    ),
            }
    )
    private float modifyEquipProgress(float equipProgress) {
        if (WorldPreview.renderingPreview) {
            return 1.0f;
        }
        return equipProgress;
    }
}
