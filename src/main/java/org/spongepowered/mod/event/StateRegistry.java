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
package org.spongepowered.mod.event;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.LoaderState;
import org.spongepowered.api.GameState;

import java.util.Map;

public class StateRegistry {

    private static Map<LoaderState, GameState> stateMap = Maps.newHashMap();

    static {
        register(LoaderState.CONSTRUCTING, GameState.CONSTRUCTION);
        register(LoaderState.PREINITIALIZATION, GameState.PRE_INITIALIZATION);
        register(LoaderState.INITIALIZATION, GameState.INITIALIZATION);
        register(LoaderState.POSTINITIALIZATION, GameState.POST_INITIALIZATION);
        register(LoaderState.AVAILABLE, GameState.LOAD_COMPLETE);

        register(LoaderState.SERVER_ABOUT_TO_START, GameState.SERVER_ABOUT_TO_START);
        register(LoaderState.SERVER_STARTING, GameState.SERVER_STARTING);
        register(LoaderState.SERVER_STARTED, GameState.SERVER_STARTED);
        register(LoaderState.SERVER_STOPPING, GameState.SERVER_STOPPING);
        register(LoaderState.SERVER_STOPPED, GameState.SERVER_STOPPED);

        // Map remaining states, just in case
        register(LoaderState.NOINIT, GameState.CONSTRUCTION);
        register(LoaderState.LOADING, GameState.CONSTRUCTION);
        register(LoaderState.ERRORED, GameState.SERVER_STOPPED);
    }

    private static void register(LoaderState state, GameState gameState) {
        stateMap.put(state, gameState);
    }

    public static GameState getState(LoaderState state) {
        return stateMap.get(state);
    }


}
