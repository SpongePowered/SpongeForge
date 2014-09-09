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
package org.spongepowered.mod.util;

import cpw.mods.fml.common.Loader;
import org.spongepowered.api.util.IGameInfo;

public class GameInfo implements IGameInfo {
    //todo: automatic replacement when build (something about manifest ??)

    private String forgeversion = Loader.instance().getFMLVersionString();
    private String minecraftversion = Loader.MC_VERSION;
    private String spongeAPIversion = "1.0.0-SNAPSHOT";
    private String spongeversion = "0.0.1"; //Truly no idea what version where on xD
    private String mcpversion = Loader.instance().getMCPVersionString();


    public String getForgeversion() {
        return forgeversion;
    }

    public String getMinecraftversion() {
        return minecraftversion;
    }

    public String getSpongeAPIversion() {
        return spongeAPIversion;
    }

    public String getSpongeversion() {
        return spongeversion;
    }

    public String getMcpversion() {
        return mcpversion;
    }

    @Override
    public String toString() {
        return "mc: " + minecraftversion + ", Sponge: " + spongeversion + "(API " + spongeAPIversion + ") (Forge: " + forgeversion + ") (MCP: " + getMcpversion() + ")";
    }
}
