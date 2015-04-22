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
package org.spongepowered.mod.mixin.core.world.storage;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.service.persistence.NbtTranslator;
import org.spongepowered.common.world.gen.WorldGeneratorRegistry;
import org.spongepowered.mod.SpongeMod;

import java.util.UUID;

@NonnullByDefault
@Mixin(WorldInfo.class)
public abstract class MixinWorldInfo implements WorldProperties, IMixinWorldInfo {

    private UUID uuid;
    private boolean worldEnabled;
    private DimensionType dimensionType;
    private boolean loadOnStartup;
    private boolean keepSpawnLoaded;
    private ImmutableCollection<String> generatorModifiers;
    private NBTTagCompound spongeRootLevelNbt;
    private NBTTagCompound spongeNbt;

    @Shadow private int dimension;

    @Shadow public abstract NBTTagCompound getNBTTagCompound();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(SpongeMod.instance.getModId(), this.spongeNbt);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(WorldSettings settings, String name, CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(SpongeMod.instance.getModId(), this.spongeNbt);

        WorldCreationSettings creationSettings = (WorldCreationSettings) (Object) settings;
        this.dimensionType = creationSettings.getDimensionType();
        this.generatorModifiers = WorldGeneratorRegistry.getInstance().toIds(creationSettings.getGeneratorModifiers());
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(NBTTagCompound nbt, CallbackInfo ci) {
        this.worldEnabled = true;
        this.spongeRootLevelNbt = new NBTTagCompound();
        this.spongeNbt = new NBTTagCompound();
        this.spongeRootLevelNbt.setTag(SpongeMod.instance.getModId(), this.spongeNbt);
    }

    @Override
    public void readSpongeNbt(NBTTagCompound nbt) {
        this.dimension = nbt.getInteger("dimensionId");
        long uuid_most = nbt.getLong("uuid_most");
        long uuid_least = nbt.getLong("uuid_least");
        this.uuid = new UUID(uuid_most, uuid_least);
        this.worldEnabled = nbt.getBoolean("enabled");
        this.keepSpawnLoaded = nbt.getBoolean("keepSpawnLoaded");
        this.loadOnStartup = nbt.getBoolean("loadOnStartup");
        for (DimensionType type : SpongeMod.instance.getSpongeRegistry().dimensionClassMappings.values()) {
            if (type.getDimensionClass().getCanonicalName().equalsIgnoreCase(nbt.getString("dimensionType"))) {
                this.dimensionType = type;
            }
        }

        // Read generator modifiers
        NBTTagList generatorModifiersNbt = nbt.getTagList("generatorModifiers", 8);
        ImmutableList.Builder<String> ids = ImmutableList.builder();
        for (int i = 0; i < generatorModifiersNbt.tagCount(); i++) {
            ids.add(generatorModifiersNbt.getStringTagAt(i));
        }
        this.generatorModifiers = ids.build();
    }

    @Override
    public DataContainer getAdditionalProperties() {
        NBTTagCompound additionalProperties = (NBTTagCompound) this.spongeRootLevelNbt.copy();
        additionalProperties.removeTag(SpongeMod.instance.getModId());
        return NbtTranslator.getInstance().translateFrom(additionalProperties);
    }

    @Override
    public void setSpongeRootLevelNBT(NBTTagCompound nbt) {
        this.spongeRootLevelNbt = nbt;
        if (nbt.hasKey(SpongeMod.instance.getModId())) {
            this.spongeNbt = nbt.getCompoundTag(SpongeMod.instance.getModId());
        } else {
            this.spongeRootLevelNbt.setTag(SpongeMod.instance.getModId(), this.spongeNbt);
        }
    }

}
