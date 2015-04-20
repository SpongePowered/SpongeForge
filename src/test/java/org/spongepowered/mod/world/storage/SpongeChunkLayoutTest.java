/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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
package org.spongepowered.mod.world.storage;

import com.flowpowered.math.vector.Vector3i;
import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.api.util.Direction;

public class SpongeChunkLayoutTest {

    @Test
    public void testConstants() {
        Assert.assertEquals(new Vector3i(16, 256, 16), SpongeChunkLayout.instance.getChunkSize());
        Assert.assertEquals(new Vector3i(1874999, 0, 1874999), SpongeChunkLayout.instance.getSpaceMax());
        Assert.assertEquals(new Vector3i(-1875000, 0, -1875000), SpongeChunkLayout.instance.getSpaceMin());
        Assert.assertEquals(new Vector3i(3750000, 1, 3750000), SpongeChunkLayout.instance.getSpaceSize());
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.getSpaceOrigin());
    }

    @Test
    public void testCoordValidation() {
        Assert.assertTrue(SpongeChunkLayout.instance.isValidChunk(new Vector3i(0, 0, 0)));
        Assert.assertTrue(SpongeChunkLayout.instance.isValidChunk(0, 0, 0));
        Assert.assertTrue(SpongeChunkLayout.instance.isValidChunk(new Vector3i(1874999, 0, 1874999)));
        Assert.assertTrue(SpongeChunkLayout.instance.isValidChunk(1874999, 0, 1874999));
        Assert.assertTrue(SpongeChunkLayout.instance.isValidChunk(new Vector3i(-1875000, 0, -1875000)));
        Assert.assertTrue(SpongeChunkLayout.instance.isValidChunk(-1875000, 0, -1875000));

        Assert.assertFalse(SpongeChunkLayout.instance.isValidChunk(1875000, 0, 1874999));
        Assert.assertFalse(SpongeChunkLayout.instance.isValidChunk(1874999, 1, 1874999));
        Assert.assertFalse(SpongeChunkLayout.instance.isValidChunk(1874999, 0, 1875000));
        Assert.assertFalse(SpongeChunkLayout.instance.isValidChunk(-1875001, 0, -1875000));
        Assert.assertFalse(SpongeChunkLayout.instance.isValidChunk(-1875000, -1, -1875000));
        Assert.assertFalse(SpongeChunkLayout.instance.isValidChunk(-1875000, 0, -1875001));
    }

    @Test
    public void testCoordConversion() {
        // chunk to world
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.toChunk(Vector3i.ZERO).get());
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.toChunk(0, 0, 0).get());
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.toChunk(new Vector3i(15, 255, 15)).get());
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.toChunk(15, 255, 15).get());

        Assert.assertEquals(new Vector3i(2, 0, 4), SpongeChunkLayout.instance.toChunk(new Vector3i(34, 121, 72)).get());
        Assert.assertEquals(new Vector3i(2, 0, 4), SpongeChunkLayout.instance.toChunk(34, 121, 72).get());

        Assert.assertEquals(new Vector3i(-6, 0, -13), SpongeChunkLayout.instance.toChunk(new Vector3i(-83, 62, -203)).get());
        Assert.assertEquals(new Vector3i(-6, 0, -13), SpongeChunkLayout.instance.toChunk(-83, 62, -203).get());

        Assert.assertFalse(SpongeChunkLayout.instance.toChunk(30000000, 0, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toChunk(-30000001, 0, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toChunk(0, 256, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toChunk(0, -1, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toChunk(0, 0, 30000000).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toChunk(0, 0, -30000001).isPresent());

        // world to chunk
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.toWorld(Vector3i.ZERO).get());
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.toWorld(0, 0, 0).get());

        Assert.assertEquals(new Vector3i(32, 0, 64), SpongeChunkLayout.instance.toWorld(new Vector3i(2, 0, 4)).get());
        Assert.assertEquals(new Vector3i(32, 0, 64), SpongeChunkLayout.instance.toWorld(2, 0, 4).get());

        Assert.assertEquals(new Vector3i(-96, 0, -208), SpongeChunkLayout.instance.toWorld(new Vector3i(-6, 0, -13)).get());
        Assert.assertEquals(new Vector3i(-96, 0, -208), SpongeChunkLayout.instance.toWorld(-6, 0, -13).get());

        Assert.assertFalse(SpongeChunkLayout.instance.toWorld(1875000, 0, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toWorld(-1875001, 0, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toWorld(0, 1, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toWorld(0, -1, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toWorld(0, 0, 1875000).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.toWorld(0, 0, -1875001).isPresent());
    }

    @Test
    public void testCoordAdd() {
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.addToChunk(Vector3i.ZERO, Vector3i.ZERO).get());
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.addToChunk(0, 0, 0, 0, 0, 0).get());

        Assert.assertEquals(new Vector3i(7, 0, 5), SpongeChunkLayout.instance.addToChunk(3, 0, 5, 4, 0, 0).get());
        Assert.assertEquals(new Vector3i(7, 0, 9), SpongeChunkLayout.instance.addToChunk(3, 0, 5, 4, 0, 4).get());

        Assert.assertFalse(SpongeChunkLayout.instance.addToChunk(1874999, 0, 0, 1, 0, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.addToChunk(0, 0, 0, 0, 1, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.addToChunk(0, 0, 1874999, 0, 0, 1).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.addToChunk(-1875000, 0, 0, -1, 0, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.addToChunk(0, 0, 0, 0, -1, 0).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.addToChunk(0, 0, -1875000, 0, 0, -1).isPresent());
    }

    @Test
    public void testCoordMove() {
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.moveToChunk(Vector3i.ZERO, Direction.NONE).get());
        Assert.assertEquals(Vector3i.ZERO, SpongeChunkLayout.instance.moveToChunk(0, 0, 0, Direction.NONE).get());

        Assert.assertEquals(new Vector3i(4, 0, 5), SpongeChunkLayout.instance.moveToChunk(3, 0, 5, Direction.EAST).get());
        Assert.assertEquals(new Vector3i(7, 0, 5), SpongeChunkLayout.instance.moveToChunk(3, 0, 5, Direction.EAST, 4).get());
        Assert.assertEquals(new Vector3i(4, 0, 6), SpongeChunkLayout.instance.moveToChunk(3, 0, 5, Direction.SOUTHEAST).get());
        Assert.assertEquals(new Vector3i(7, 0, 9), SpongeChunkLayout.instance.moveToChunk(3, 0, 5, Direction.SOUTHEAST, 4).get());

        Assert.assertFalse(SpongeChunkLayout.instance.moveToChunk(1874999, 0, 0, Direction.EAST).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.moveToChunk(0, 0, 0, Direction.UP).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.moveToChunk(0, 0, 1874999, Direction.SOUTH).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.moveToChunk(-1875000, 0, 0, Direction.WEST).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.moveToChunk(0, 0, 0, Direction.DOWN).isPresent());
        Assert.assertFalse(SpongeChunkLayout.instance.moveToChunk(0, 0, -1875000, Direction.NORTH).isPresent());

        try {
            SpongeChunkLayout.instance.moveToChunk(0, 0, 0, Direction.SOUTH_SOUTHEAST);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
            // we expect this exception
        }
    }

}
