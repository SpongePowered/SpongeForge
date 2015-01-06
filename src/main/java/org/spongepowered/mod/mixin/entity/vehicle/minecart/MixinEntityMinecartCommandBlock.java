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
package org.spongepowered.mod.mixin.entity.vehicle.minecart;

import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import org.spongepowered.api.entity.vehicle.minecart.MinecartCommandBlock;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityMinecartCommandBlock.class)
@Implements(@Interface(iface = MinecartCommandBlock.class, prefix = "commandminecartminecart$"))
public abstract class MixinEntityMinecartCommandBlock extends EntityMinecart {

    @Shadow
    private CommandBlockLogic field_145824_a;

    public MixinEntityMinecartCommandBlock(World worldIn) {
        super(worldIn);
    }

    public String commandminecart$getCommand() {
        return field_145824_a.getName();
    }

    public void commandminecart$setCommand(@Nonnull String command) {
        field_145824_a.setCommand(command);
    }

    public String commandminecart$getCommandName() {
        return field_145824_a.getCustomName();
    }

    public void commandminecartsetCommandName(@Nullable String name) {
        if (name == null)
            name = "@";
        field_145824_a.func_145754_b(name);
    }

}
