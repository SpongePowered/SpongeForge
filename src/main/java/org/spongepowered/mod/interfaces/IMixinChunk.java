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
package org.spongepowered.mod.interfaces;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IMixinChunk {

    Cause getCurrentPopulateCause();

    Map<Short, Integer> getTrackedShortPlayerPositions();

    Map<Integer, Integer> getTrackedIntPlayerPositions();

    Optional<UUID> getTrackedPlayerUniqueId(BlockPos pos);

    Optional<User> getBlockPosOwner(BlockPos pos);

    IBlockState setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, BlockSnapshot newBlockSnapshot);

    void addTrackedBlockPosition(Block block, BlockPos pos, EntityPlayer processingPlayer);

    void setTrackedIntPlayerPositions(Map<Integer, Integer> trackedPlayerPositions);

    void setTrackedShortPlayerPositions(Map<Short, Integer> trackedPlayerPositions);

    void removeTrackedPlayerPosition(BlockPos pos);
}
