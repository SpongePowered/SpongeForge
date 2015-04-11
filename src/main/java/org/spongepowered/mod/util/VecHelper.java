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
package org.spongepowered.mod.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Rotations;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

public final class VecHelper {

    // === Flow Vector3d --> BlockPos ===

    public static BlockPos toBlockPos(Vector3d vector) {
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector3i --> BlockPos ===

    public static BlockPos toBlockPos(Vector3i vector) {
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }

    // === MC BlockPos --> Flow Vector3i ==

    public static Vector3i toVector(BlockPos pos) {
        return new Vector3i(pos.getX(), pos.getY(), pos.getZ());
    }

    // === MC BlockPos --> Flow Vector3d ==

    public static Vector3d toVector3d(BlockPos pos) {
        return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }

    // === Rotations --> Flow Vector ===

    public static Vector3d toVector(Rotations rotation) {
        return new Vector3d(rotation.getX(), rotation.getY(), rotation.getZ());
    }

    // === MC Vec3i --> Flow Vector3i ===

    public static Vector3i toVector(Vec3i vector) {
        return new Vector3i(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector3i --> MC Vec3i ===

    public static Vec3i toVector(Vector3i vector) {
        return new Vec3i(vector.getX(), vector.getY(), vector.getZ());
    }

    // === MC Vec3 --> flow Vector3d ==

    public static Vector3d toVector(Vec3 vector) {
        return new Vector3d(vector.xCoord, vector.yCoord, vector.zCoord);
    }

    // === Flow Vector3d --> MC Vec3 ==

    public static Vec3 toVector(Vector3d vector) {
        return new Vec3(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector --> Rotations ===
    public static Rotations toRotation(Vector3d vector) {
        return new Rotations((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
    }
}
