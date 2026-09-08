package me.contaria.seedqueue.mixin.included.worldpreview.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.contaria.seedqueue.worldpreview.WorldPreview;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    private ItemStack heldItem;

    @ModifyExpressionValue(
            method = "renderHeldItemName",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;heldItem:Lnet/minecraft/item/ItemStack;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ItemStack modifyCurrentStack(ItemStack currentStack) {
        if (WorldPreview.renderingPreview) {
            return WorldPreview.properties.player.getMainHandStack();
        }
        return currentStack;
    }

    @ModifyExpressionValue(
            method = "renderHeldItemName",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;heldItemTooltipFade:I",
                    opcode = Opcodes.GETFIELD
            )
    )
    private int modifyHeldItemTooltipFade(int heldItemTooltipFade) {
        if (WorldPreview.renderingPreview) {
            // see InGameHud#tick, the check needs to be performed because vanilla doesn't reset InGameHud#currentStack when changing worlds
            ItemStack itemStack = WorldPreview.properties.player.getMainHandStack();
            if (itemStack != null && this.heldItem != null && itemStack.getItem() == this.heldItem.getItem() && ItemStack.equalsIgnoreDamage(itemStack, this.heldItem) && (itemStack.isDamageable() || itemStack.getData() == this.heldItem.getData())) {
                return heldItemTooltipFade;
            }
            return 40;
        }
        return heldItemTooltipFade;
    }
}
