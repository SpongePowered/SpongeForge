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
import net.minecraft.command.server.CommandBlockLogic;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.CommandBlockSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.interfaces.Subjectable;
import org.spongepowered.mod.text.SpongeText;
import org.spongepowered.mod.util.VecHelper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(CommandBlockLogic.class)
public abstract class MixinCommandBlockLogic implements ICommandSender, CommandBlockSource, Subjectable {

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
    public Location getLocation() {
        return new Location((Extent) getEntityWorld(), VecHelper.toVector(getPositionVector()));
    }

    @Override
    public org.spongepowered.api.world.World getWorld() {
        return (org.spongepowered.api.world.World) getEntityWorld();
    }

    @Override
    public String getIdentifier() {
        return getName();
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_COMMAND_BLOCK;
    }

    @Override
    public Tristate permDefault(String permission) {
        return Tristate.TRUE;
    }
}
