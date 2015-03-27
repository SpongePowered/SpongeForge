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
package org.spongepowered.mod.mixin.api.text.title;

import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S45PacketTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.SpongeText;
import org.spongepowered.mod.text.title.SpongeTitle;

import java.util.Arrays;

@Mixin(value = Title.class, remap = false)
public abstract class MixinTitle implements SpongeTitle {

    @Shadow protected Optional<Text> title;
    @Shadow protected Optional<Text> subtitle;
    @Shadow protected Optional<Integer> fadeIn;
    @Shadow protected Optional<Integer> stay;
    @Shadow protected Optional<Integer> fadeOut;
    @Shadow protected boolean clear;
    @Shadow protected boolean reset;

    private S45PacketTitle[] packets;

    @Override
    public void send(EntityPlayerMP player) {
        if (this.packets == null) {
            S45PacketTitle[] packets = new S45PacketTitle[5];
            int i = 0;

            if (this.clear) {
                packets[i++] = new S45PacketTitle(S45PacketTitle.Type.CLEAR, null);
            }
            if (this.reset) {
                packets[i++] = new S45PacketTitle(S45PacketTitle.Type.RESET, null);
            }
            if (this.fadeIn.isPresent() || this.stay.isPresent() || this.fadeOut.isPresent()) {
                packets[i++] = new S45PacketTitle(this.fadeIn.or(20), this.stay.or(60), this.fadeOut.or(20));
            }
            if (this.subtitle.isPresent()) {
                packets[i++] = new S45PacketTitle(S45PacketTitle.Type.SUBTITLE, ((SpongeText) this.subtitle.get()).toComponent());
            }
            if (this.title.isPresent()) {
                packets[i++] = new S45PacketTitle(S45PacketTitle.Type.TITLE, ((SpongeText) this.title.get()).toComponent());
            }

            this.packets = i == packets.length ? packets : Arrays.copyOf(packets, i);
        }

        for (S45PacketTitle packet : this.packets) {
            player.playerNetServerHandler.sendPacket(packet);
        }
    }
}
