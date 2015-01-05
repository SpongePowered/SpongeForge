package org.spongepowered.mod.mixin.command.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@NonnullByDefault
@Mixin(value=net.minecraft.command.server.CommandBlockLogic.class,remap=false)
public abstract class MixinCommandBlockLogic implements ICommandSender, CommandSource {

	@Shadow public abstract void addChatMessage(IChatComponent icc);
	
	@Override
	public void sendMessage(String... messages) {
		String s=new String();
		for(String text:messages){
			s += text;
		}
		this.addChatMessage(new ChatComponentText(s));
	}

	@Override
	public void sendMessage(Message... messages) {
		List<String> s=new ArrayList<String>();
		for(Message message:messages){
			if(message instanceof Message.Text)
				s.add((String)message.getContent());
		}
		this.sendMessage(s.toArray(new String[s.size()]));
	}

	@Override
	public void sendMessage(Iterable<Message> messages) {
		List<Message> s=new ArrayList<Message>();
		for(Message message:messages){
			s.add(message);
		}
		this.sendMessage(s.toArray(new Message[s.size()]));
	}
	
}
