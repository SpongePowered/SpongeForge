/**
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2014 SpongePowered <http://spongepowered.org/>
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
package org.spongepowered.mod;

import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.Block;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import java.io.*;
import java.util.UUID;

public class SpongeWorld implements World {
    private final WorldServer handle;
    private final UUID uuid;

    public SpongeWorld(WorldServer handle) {
        this.handle = handle;
        this.uuid = findUUID();
    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

    @Override
    public String getName() {
        return handle.getWorldInfo().getWorldName();
    }

    @Override
    public Chunk getChunk(int x, int z) {
        return null;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return null;
    }

    private UUID findUUID() {
        //Look for uuid in world folder.
        File file = new File(handle.getSaveHandler().getWorldDirectory(), "uuid.dat");
        if(file.exists()) {
            DataInputStream dataInputStream = null;
            try {
                dataInputStream = new DataInputStream(new FileInputStream(file));
                return new UUID(dataInputStream.readLong(), dataInputStream.readLong());
            } catch(IOException e) {
                //TODO: Add error message
            } finally {
                if(dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        UUID uuid = UUID.randomUUID();
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(new FileOutputStream(file));
            dataOutputStream.writeLong(uuid.getMostSignificantBits());
            dataOutputStream.writeLong(uuid.getLeastSignificantBits());
        } catch(IOException e) {
            //TODO: Add error message
        } finally {
            if(dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                }
            }
        }

        return uuid;
    }
}
