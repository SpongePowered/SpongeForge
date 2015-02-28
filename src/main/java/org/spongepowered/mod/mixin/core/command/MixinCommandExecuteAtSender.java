package org.spongepowered.mod.mixin.core.command;

import com.google.common.base.Optional;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.text.message.SpongeMessage;

import java.util.List;
import java.util.Set;

@Mixin(targets = "net/minecraft/command/CommandExecuteAt$1")
@NonnullByDefault
public abstract class MixinCommandExecuteAtSender implements CommandSource, ICommandSender {

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            addChatMessage(new ChatComponentText(message));
        }
    }

    @Override
    public void sendMessage(Message... messages) {
        for (Message message : messages) {
            addChatMessage(((SpongeMessage<?>) message).getHandle());
        }
    }

    @Override
    public void sendMessage(Iterable<Message> messages) {
        for (Message message : messages) {
            addChatMessage(((SpongeMessage<?>) message).getHandle());
        }
    }

    @Override
    public String getIdentifier() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public SubjectCollection getContainingCollection() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public SubjectData getData() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public SubjectData getTransientData() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public boolean hasPermission(String permission) {
        return true; // TODO
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public boolean isChildOf(Subject parent) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public List<Subject> getParents() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Set<Context> getActiveContexts() {
        throw new UnsupportedOperationException(); // TODO
    }

}
