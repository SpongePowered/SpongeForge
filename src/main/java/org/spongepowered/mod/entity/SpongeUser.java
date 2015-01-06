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
package org.spongepowered.mod.entity;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.mod.SpongeMod;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;

public abstract class SpongeUser extends EntityPlayer implements User {

    public SpongeUser(World worldIn, GameProfile profile) {
        super(worldIn, profile);
    }

    @Override
    public String getName() {
        return getGameProfile().getName();
    }

    @Override
    public boolean hasJoinedBefore() {
        return this.getData().isPresent();
    }

    @Override
    public Date getFirstPlayed() {
        Optional<NBTTagCompound> data = getData();
        long time = (data.isPresent() && data.get().hasKey("firstPlayed")) ? data.get().getLong("firstPlayed") : 0L;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.getTime();
    }

    @Override
    public Date getLastPlayed() {
        Optional<NBTTagCompound> data = getData();
        long time = (data != null && data.get().hasKey("lastPlayed")) ? data.get().getLong("lastPlayed") : 0L;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.getTime();
    }

    @Override
    public boolean isBanned() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return server.getConfigurationManager().getBannedPlayers().isBanned(getGameProfile());
    }

    @Override
    public boolean isWhitelisted() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return server.getConfigurationManager().getWhitelistedPlayers().func_152705_a(getGameProfile());
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isPresent();
    }

    @Override
    public Optional<Player> getPlayer() {
        Optional<Server> server = SpongeMod.instance.getGame().getServer();
        if (server.isPresent()) {
            return server.get().getPlayer(getName());
        }
        return Optional.absent();
    }

    private Optional<NBTTagCompound> getData() {
        MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        SaveHandler saveHandler = (SaveHandler)mcServer.worldServers[0].getSaveHandler();
        Optional<Player> player = getPlayer();
        if (player.isPresent()) {
            return Optional.fromNullable(saveHandler.readPlayerData((EntityPlayer) player.get()));
        }
        return Optional.absent();
    }
}
