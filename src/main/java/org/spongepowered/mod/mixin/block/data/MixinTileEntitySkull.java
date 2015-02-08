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
package org.spongepowered.mod.mixin.block.data;

import com.google.common.base.Optional;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.block.data.Skull;
import org.spongepowered.api.block.meta.SkullType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;

@NonnullByDefault
@Implements(@Interface(iface = Skull.class, prefix = "skull$"))
@Mixin(net.minecraft.tileentity.TileEntitySkull.class)
public abstract class MixinTileEntitySkull extends TileEntity {

    @Shadow
    public abstract void setPlayerProfile(com.mojang.authlib.GameProfile playerProfile);

    @Shadow
    public abstract com.mojang.authlib.GameProfile getPlayerProfile();

    @Shadow
    public abstract void setType(int type);

    @Shadow
    public abstract int getSkullType();

    @Shadow
    private int skullRotation;

    public Direction skull$getRotation() {
        return null; //TODO
    }

    public void skull$setRotation(Direction rotation) {
        //TODO
    }

    public Optional<GameProfile> skull$getPlayer() {
        return Optional.fromNullable((GameProfile) getPlayerProfile());
    }

    public void skull$setPlayer(GameProfile player) {
        setPlayerProfile((com.mojang.authlib.GameProfile) player);
    }

    public SkullType skull$getType() {
        return SpongeMod.instance.getGame().getRegistry().getSkullTypes().get(getSkullType());
    }

    public void skull$setType(SkullType type) {
        setType(type.getId());
    }

}
