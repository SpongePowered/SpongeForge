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
package org.spongepowered.mod.effect.particle;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.EnumParticleTypes;
import org.spongepowered.api.item.ItemBlock;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class SpongeParticleHelper {

    /**
     * Gets the list of packets that are needed to spawn the particle effect at the position.
     * This method tries to minimize the amount of packets for better performance and
     * lower bandwidth use.
     *
     * @param effect The particle effect
     * @param position The position
     * @return The packets
     */
    public static List<Packet> toPackets(SpongeParticleEffect effect, Vector3d position) {
        SpongeParticleType type = effect.getType();
        EnumParticleTypes internal = type.getInternalType();

        Vector3f offset = effect.getOffset();

        int count = effect.getCount();
        int[] extra = new int[0];

        float px = (float) position.getX();
        float py = (float) position.getY();
        float pz = (float) position.getZ();

        float ox = offset.getX();
        float oy = offset.getY();
        float oz = offset.getZ();

        // The extra values, normal behavior offsetX, offsetY, offsetZ
        float f0 = 0f;
        float f1 = 0f;
        float f2 = 0f;

        // Depends on behavior
        // Note: If the count > 0 -> speed = 0f else if count = 0 -> speed = 1f

        if (effect instanceof SpongeParticleEffect.Materialized) {
            ItemStack item = ((SpongeParticleEffect.Materialized) effect).getItem();
            ItemType itemType = item.getItem();

            int id = 0;
            int data = 0;

            if (internal == EnumParticleTypes.ITEM_CRACK) {
                id = Item.itemRegistry.getIDForObject(itemType);
                data = item.getDamage();
            } else if (internal == EnumParticleTypes.BLOCK_CRACK || internal == EnumParticleTypes.BLOCK_DUST) {
                // Only block types are allowed
                if (itemType instanceof ItemBlock) {
                    id = Block.blockRegistry.getIDForObject(((ItemBlock) itemType).getBlock());
                    data = item.getDamage();
                }
            }

            if (id == 0) {
                return Collections.emptyList();
            }

            extra = new int[]{id, data};
        }

        if (effect instanceof SpongeParticleEffect.Resized) {
            float size = ((SpongeParticleEffect.Resized) effect).getSize();

            // The formula of the large explosion acts strange
            // Client formula: sizeClient = 1 - sizeServer * 0.5
            // The particle effect returns the client value so
            // Server formula: sizeServer = (-sizeClient * 2) + 2
            if (internal == EnumParticleTypes.EXPLOSION_LARGE) {
                size = (-size * 2f) + 2f;
            }

            if (size == 0f) {
                return Lists.<Packet>newArrayList(new S2APacketParticles(internal, true, px, py, pz, ox, oy, oz, 0f, count, extra));
            }

            f0 = size;
        } else if (effect instanceof SpongeParticleEffect.Colored) {
            Color color0 = ((SpongeParticleEffect.Colored) effect).getColor();
            Color color1 = ((SpongeParticleType.Colorable) type).getDefaultColor();

            if (color0.equals(color1)) {
                return Lists.<Packet>newArrayList(new S2APacketParticles(internal, true, px, py, pz, ox, oy, oz, 0f, count, extra));
            }

            f0 = color0.getRed() / 255f;
            f1 = color0.getGreen() / 255f;
            f2 = color0.getBlue() / 255f;

            // If the f0 value 0 is, the redstone will set it automatically to red 255
            if (f0 == 0f && internal == EnumParticleTypes.REDSTONE) {
                f0 = 0.00001f;
            }
        } else if (effect instanceof SpongeParticleEffect.Note) {
            float note = ((SpongeParticleEffect.Note) effect).getNote();

            if (note == 0f) {
                return Lists.<Packet>newArrayList(new S2APacketParticles(internal, true, px, py, pz, ox, oy, oz, 0f, count, extra));
            }

            f0 = note / 24f;
        } else if (type.hasMotion()) {
            Vector3f motion = effect.getMotion();

            float mx = motion.getX();
            float my = motion.getY();
            float mz = motion.getZ();

            // The y value won't work for this effect, if the value isn't 0 the motion won't work
            if (internal == EnumParticleTypes.WATER_SPLASH) {
                my = 0f;
            }

            if (mx == 0f && my == 0f && mz == 0f) {
                return Lists.<Packet>newArrayList(new S2APacketParticles(internal, true, px, py, pz, ox, oy, oz, 0f, count, extra));
            } else {
                f0 = mx;
                f1 = my;
                f2 = mz;
            }
        }

        // Is this check necessary?
        if (f0 == 0f && f1 == 0f && f2 == 0f) {
            return Lists.<Packet>newArrayList(new S2APacketParticles(internal, true, px, py, pz, ox, oy, oz, 0f, count, extra));
        }

        List<Packet> packets = Lists.newArrayList();

        if (ox == 0f && oy == 0f && oz == 0f) {
            for (int i = 0; i < count; i++) {
                packets.add(new S2APacketParticles(internal, true, px, py, pz, f0, f1, f2, 1f, 0, extra));
            }
        } else {
            Random random = new Random();

            for (int i = 0; i < count; i++) {
                float px0 = (float) (px + (random.nextFloat() * 2f - 1f) * ox);
                float py0 = (float) (py + (random.nextFloat() * 2f - 1f) * oy);
                float pz0 = (float) (pz + (random.nextFloat() * 2f - 1f) * oz);

                packets.add(new S2APacketParticles(internal, true, px0, py0, pz0, f0, f1, f2, 1f, 0, extra));
            }
        }

        return packets;
    }

    private SpongeParticleHelper() {
    }
}
