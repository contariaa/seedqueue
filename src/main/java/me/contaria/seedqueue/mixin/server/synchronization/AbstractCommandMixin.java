package me.contaria.seedqueue.mixin.server.synchronization;

import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandProvider;
import net.minecraft.server.MinecraftServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractCommand.class)
public abstract class AbstractCommandMixin {

    @Redirect(
            method = "run(Lnet/minecraft/command/CommandSource;Lnet/minecraft/command/Command;ILjava/lang/String;[Ljava/lang/Object;)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/command/AbstractCommand;commandProvider:Lnet/minecraft/command/CommandProvider;",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static CommandProvider useServerCommandManager() {
        return (CommandProvider) MinecraftServer.getServer().getCommandManager();
    }
}
