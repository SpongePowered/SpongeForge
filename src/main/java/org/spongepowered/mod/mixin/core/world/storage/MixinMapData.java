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
package org.spongepowered.mod.mixin.core.world.storage;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.spongepowered.api.item.data.map.MapData;
import org.spongepowered.api.item.data.map.MapRenderer;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(net.minecraft.world.storage.MapData.class)
public abstract class MixinMapData extends WorldSavedData implements MapData {
    @Shadow
    public int xCenter;

    @Shadow
    public int zCenter;

    @Shadow
    public int dimension;

    @Shadow
    public byte scale;

    @Shadow
    public byte[] colors = new byte[16384];

    @Shadow
    public String mapName;

    @Shadow
    public List playersArrayList;

    @Shadow
    private Map playersHashMap;

    @Shadow
    public Map playersVisibleOnMap;

    private World spongeWorld;

    private boolean isVirtual;

    private List<MapRenderer> rendererList = Lists.newArrayList();

    public MixinMapData(String name) {
        super(name);
    }

    @Override
    @Overwrite
    // TODO: REPLACE OVERWRITE
    public void readFromNBT(NBTTagCompound nbt)
    {
        net.minecraft.nbt.NBTBase dimension = nbt.getTag("dimension");

        if (dimension instanceof net.minecraft.nbt.NBTTagByte)
        {
            this.dimension = ((net.minecraft.nbt.NBTTagByte)dimension).getByte();
        }
        else
        {
            this.dimension = ((net.minecraft.nbt.NBTTagInt)dimension).getInt();
        }

        if (!nbt.hasKey("spongeWorld") && !nbt.hasKey("isVirtual")) {
            WorldServer worldServer = DimensionManager.getWorld(this.dimension);
            UUID worldUUID = ((World) worldServer).getUniqueId();
            nbt.setLong("spongeWorldUUIDLower", worldUUID.getLeastSignificantBits());
            nbt.setLong("spongeWorldUUIDUpper", worldUUID.getMostSignificantBits());
            nbt.setBoolean("spongeWorld", true);
            this.spongeWorld = (World) worldServer;
        } else if (nbt.hasKey("spongeWorld") && !nbt.hasKey("isVirtual")){
            long lower = nbt.getLong("spongeWorldUUIDLower");
            long upper = nbt.getLong("spongeWorldUUIDUpper");
            UUID uuid = new UUID(upper, lower);
            World world = SpongeMod.instance.getGame().getServer().get().getWorld(uuid).get();
            this.spongeWorld = world;
            this.dimension = world.getDimension().getDimensionId();
        }

        if (!nbt.hasKey("isVirtual")) {
            nbt.setBoolean("isVirtual", false);
            isVirtual = false;
        } else {
            isVirtual = nbt.getBoolean("isVirtual");
        }


        this.xCenter = nbt.getInteger("xCenter");
        this.zCenter = nbt.getInteger("zCenter");
        this.scale = nbt.getByte("scale");
        this.scale = (byte) MathHelper.clamp_int(this.scale, 0, 4);
        short short1 = nbt.getShort("width");
        short short2 = nbt.getShort("height");

        if (short1 == 128 && short2 == 128)
        {
            this.colors = nbt.getByteArray("colors");
        }
        else
        {
            byte[] abyte = nbt.getByteArray("colors");
            this.colors = new byte[16384];
            int i = (128 - short1) / 2;
            int j = (128 - short2) / 2;

            for (int k = 0; k < short2; ++k)
            {
                int l = k + j;

                if (l >= 0 || l < 128)
                {
                    for (int i1 = 0; i1 < short1; ++i1)
                    {
                        int j1 = i1 + i;

                        if (j1 >= 0 || j1 < 128)
                        {
                            this.colors[j1 + l * 128] = abyte[i1 + k * short1];
                        }
                    }
                }
            }
        }
    }

    @Override
    @Overwrite
    // TODO: REPLACE OVERWRITE
    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("dimension", this.dimension);
        nbt.setBoolean("isVirtual", isVirtual);
        nbt.setBoolean("spongeWorld", true);
        nbt.setLong("spongeWorldUUIDLower", spongeWorld.getUniqueId().getLeastSignificantBits());
        nbt.setLong("spongeWorldUUIDUpper", spongeWorld.getUniqueId().getMostSignificantBits());
        nbt.setInteger("xCenter", this.xCenter);
        nbt.setInteger("zCenter", this.zCenter);
        nbt.setByte("scale", this.scale);
        nbt.setShort("width", (short)128);
        nbt.setShort("height", (short)128);
        nbt.setByteArray("colors", this.colors);
    }

    @Override
    @Overwrite
    // TODO: NEED HELP
    public void updateVisiblePlayers(EntityPlayer player, ItemStack mapStack)
    {
        if (!this.playersHashMap.containsKey(player))
        {
            MapData.MapInfo mapinfo = new MapData.MapInfo(player);
            this.playersHashMap.put(player, mapinfo);
            this.playersArrayList.add(mapinfo);
        }

        if (!player.inventory.hasItemStack(mapStack))
        {
            this.playersVisibleOnMap.remove(player.getCommandSenderName());
        }

        for (int i = 0; i < this.playersArrayList.size(); ++i)
        {
            MapData.MapInfo mapinfo1 = (MapData.MapInfo)this.playersArrayList.get(i);

            if (!mapinfo1.entityplayerObj.isDead && (mapinfo1.entityplayerObj.inventory.hasItemStack(mapStack) || mapStack.isOnItemFrame()))
            {
                if (!mapStack.isOnItemFrame() && mapinfo1.entityplayerObj.dimension == this.dimension)
                {
                    this.updatePlayersVisibleOnMap(0, mapinfo1.entityplayerObj.worldObj, mapinfo1.entityplayerObj.getCommandSenderName(), mapinfo1.entityplayerObj.posX, mapinfo1.entityplayerObj.posZ, (double)mapinfo1.entityplayerObj.rotationYaw);
                }
            }
            else
            {
                this.playersHashMap.remove(mapinfo1.entityplayerObj);
                this.playersArrayList.remove(mapinfo1);
            }
        }

        if (mapStack.isOnItemFrame())
        {
            EntityItemFrame entityitemframe = mapStack.getItemFrame();
            BlockPos blockpos = entityitemframe.getHangingPosition();
            this.updatePlayersVisibleOnMap(1, player.worldObj, "frame-" + entityitemframe.getEntityId(), (double)blockpos.getX(), (double)blockpos.getZ(), (double)(entityitemframe.facingDirection.getHorizontalIndex() * 90));
        }

        if (mapStack.hasTagCompound() && mapStack.getTagCompound().hasKey("Decorations", 9))
        {
            NBTTagList nbttaglist = mapStack.getTagCompound().getTagList("Decorations", 10);

            for (int j = 0; j < nbttaglist.tagCount(); ++j)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(j);

                if (!this.playersVisibleOnMap.containsKey(nbttagcompound.getString("id")))
                {
                    this.updatePlayersVisibleOnMap(nbttagcompound.getByte("type"), player.worldObj, nbttagcompound.getString("id"), nbttagcompound.getDouble("x"), nbttagcompound.getDouble("z"), nbttagcompound.getDouble("rot"));
                }
            }
        }
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.fromNullable(spongeWorld);
    }

    @Override
    public void setWorld(World world) {
        this.spongeWorld = world;
    }

    @Override
    public String getId() {
        return mapName;
    }

    @Override
    public int getScale() {
        return scale;
    }

    @Override
    public void setScale(int scale) {
        this.scale = (byte) MathHelper.clamp_int(scale, 0, 4);
    }

    @Override
    public int getWidth() {
        // Currently hardcoded in implementation
        return 128;
    }

    @Override
    public int getHeight() {
        // Currently hardcoded in implementation
        return 128;
    }

    @Override
    public Vector2i getCenter() {
        return new Vector2i(xCenter, zCenter);
    }

    @Override
    public void setCenter(Vector2i center) {
        xCenter = center.getX();
        zCenter = center.getY();
    }

    @Override
    public boolean isVirtual() {
        return isVirtual;
    }

    @Override
    public List<MapRenderer> getAllRenderers() {
        return rendererList;
    }

    @Override
    public void addMapRenderer(MapRenderer renderer) {
        rendererList.add(renderer);
    }

    @Override
    public void removeMapRenderer(MapRenderer renderer) {
        rendererList.remove(renderer);
    }
}
