/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.mod.mixin.core.command;

import com.google.common.base.Optional;
import net.minecraft.command.ICommandSender;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.text.SpongeText;

import java.util.List;
import java.util.Set;

@Mixin(targets = "net/minecraft/command/CommandExecuteAt$1")
@NonnullByDefault
public abstract class MixinCommandExecuteAtSender implements CommandSource, ICommandSender {

    @Override
    public void sendMessage(Text... messages) {
        for (Text message : messages) {
            addChatMessage(((SpongeText) message).toComponent());
        }
    }

    @Override
    public void sendMessage(Iterable<Text> messages) {
        for (Text message : messages) {
            addChatMessage(((SpongeText) message).toComponent());
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
