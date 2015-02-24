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
package org.spongepowered.mod.mixin.core.block.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.block.data.Sign;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.message.SpongeMessage;

@NonnullByDefault
@Implements(@Interface(iface = Sign.class, prefix = "sign$"))
@Mixin(net.minecraft.tileentity.TileEntitySign.class)
public abstract class MixinTileEntitySign extends TileEntity {

    @Shadow
    public IChatComponent[] signText;

    public Message[] sign$getLines() {
        return new Message[] {SpongeMessage.of(this.signText[0]), SpongeMessage.of(this.signText[1]), SpongeMessage.of(this.signText[2]),
                SpongeMessage.of(this.signText[3])};
    }

    public void sign$setLines(Message... lines) {
        checkArgument(lines.length <= 4, "Only 4 lines can be entered on a sign!");
        for (int i = 0; i < lines.length; i++) {
            this.signText[i] = ((SpongeMessage) lines[i]).getHandle();
        }
    }

    public Message sign$getLine(int index) throws IndexOutOfBoundsException {
        checkElementIndex(index, this.signText.length);

        return SpongeMessage.of(this.signText[index]);
    }

    public void sign$setLine(int index, Message text) throws IndexOutOfBoundsException {
        checkElementIndex(index, this.signText.length);

        this.signText[index] = ((SpongeMessage) text).getHandle();
    }

}
