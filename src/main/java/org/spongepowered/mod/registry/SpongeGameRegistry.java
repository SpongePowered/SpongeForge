/**
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

package org.spongepowered.mod.registry;

import com.google.common.base.Optional;

import net.minecraftforge.fml.common.registry.GameData;

import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.effect.Particle;
import org.spongepowered.api.effect.Particles;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.effect.SpongeParticle;
import org.spongepowered.mod.effect.SpongeParticleFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
public class SpongeGameRegistry implements GameRegistry {

    @Override
    public Optional<BlockType> getBlock(String id) {
        return Optional.fromNullable((BlockType) GameData.getBlockRegistry().getObject(id));
    }

    @Override
    public Optional<ItemType> getItem(String id) {
        return Optional.fromNullable((ItemType) GameData.getItemRegistry().getObject(id));
    }

    @Override
    public Optional<String> getId(Object obj) {
        if (obj instanceof BlockType) {
            @Nullable
            Object key = GameData.getBlockRegistry().getNameForObject(obj);

            if(key == null) {
                return Optional.absent();
            } else {
                return Optional.fromNullable(key.toString());
            }
        } else if (obj instanceof ItemType) {
            @Nullable
            Object key = GameData.getItemRegistry().getNameForObject(obj);

            if(key == null) {
                return Optional.absent();
            } else {
                return Optional.fromNullable(key.toString());
            }
        }

        throw new IllegalArgumentException("Object is not BlockType or ItemType.");
    }

    // Note: This is probably fairly slow, but only needs to be run rarely.
    public void setBlockTypes() {
        for (Field f : BlockTypes.class.getDeclaredFields()) {
            try {
                f.set(null, (BlockType) GameData.getBlockRegistry().getObject(f.getName().toLowerCase()));
            } catch (Exception e) {
                // Ignoring error
            }
        }
    }

    // Note: This is probably fairly slow, but only needs to be run rarely.
    public void setItemTypes() {
        for (Field f : ItemTypes.class.getDeclaredFields()) {
            try {
                f.set(null, (ItemType) GameData.getItemRegistry().getObject(f.getName().toLowerCase()));
            } catch (Exception e) {
                // Ignoring error
            }
        }
    }
    
    // Note: This is fairly slow.
    public void setParticles() {
        List<Particle> particles = new ArrayList<Particle>();
        
        for (Field f : Particles.class.getDeclaredFields()) {
            if (f.getName().equals("factory")) {
                // Don't set factory here
                continue;
            }
            
            Particle particle = new SpongeParticle(f.getName().toLowerCase());
            particles.add(particle);
            try {
                f.set(null, particle);
            } catch (Exception e) {
                // Ignoring error
            }
        }
        
        try {
            Particles.class.getField("factory").set(null, new SpongeParticleFactory(particles));
        } catch (Exception e) {
            // Ignoring error
        }
    }
}
