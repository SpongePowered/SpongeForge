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
package org.spongepowered.mod.util;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Rotations;
import net.minecraft.util.Vec3i;

public final class VecHelper {

    // === Flow Vector --> BlockPos ===

    public static BlockPos toBlockPos(Vector3d vector) {
        return new BlockPos(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Flow Vector --> Rotations ===

    public static Rotations toRotation(Vector3f vector) {
        return new Rotations(vector.getX(), vector.getY(), vector.getZ());
    }

    // === Rotations --> Flow Vector ===

    public static Vector3f toVector(Rotations rotation) {
        return new Vector3f(rotation.func_179415_b(), rotation.func_179416_c(), rotation.func_179413_d());
    }

    // === MC Vector --> Flow Vector ===

    public static Vector3i toVector(Vec3i vector) {
        return new Vector3i(vector.getX(), vector.getY(), vector.getZ());
    }
}
