package org.spongepowered.mod.mixin.command.server;

import net.minecraft.command.ICommandSender;

import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;

@NonnullByDefault
@Mixin(net.minecraft.command.server.CommandBlockLogic.class)
public abstract class MixinCommandBlockLogic implements ICommandSender, CommandSource{

	@Override
	public void sendMessage(String... messages) {
		// Do nothing
	}

	@Override
	public void sendMessage(Message... messages) {
		// Do nothing
	}

	@Override
	public void sendMessage(Iterable<Message> messages) {
		// Do nothing
	}
	
}
