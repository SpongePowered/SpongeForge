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
package org.spongepowered.mod.mixin.core.event.entity.living;

import com.google.common.collect.ImmutableList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.util.StaticMixinHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(value = LivingDropsEvent.class, remap = false)
public abstract class MixinEventLivingDrops extends MixinEventLiving implements DropItemEvent.Destruct {

    protected EntitySnapshot entitySnapshot;
    protected ImmutableList<EntitySnapshot> entitySnapshots;
    private Cause cause;

    @Shadow @Final public DamageSource source;
    @Shadow @Final public List<EntityItem> drops;
    @Shadow @Final public int lootingLevel;
    @Shadow @Final public boolean recentlyHit;

    @Inject(method = "<init>", at = @At("RETURN") )
    public void onConstructed(EntityLivingBase entity, DamageSource source, List<EntityItem> drops, int lootingLevel, boolean recentlyHit, CallbackInfo ci) {
        if (StaticMixinHelper.processingInternalForgeEvent) {
            return;
        }

        if (!entity.worldObj.isRemote) { // ignore client
            ImmutableList.Builder<EntitySnapshot> builder = new ImmutableList.Builder<>();
            for (EntityItem entityItem : drops) {
                builder.add(((Entity) entityItem).createSnapshot());
            }

            this.entitySnapshots = builder.build();
            System.out.println("Entity = " + entity + ", source = " + source);
            this.cause = Cause.of(NamedCause.source(entity), NamedCause.of("Attacker", this.source));
        }
    }

    public void syncDataToSponge(net.minecraftforge.fml.common.eventhandler.Event forgeEvent) {
        net.minecraftforge.event.entity.living.LivingDropsEvent event = (net.minecraftforge.event.entity.living.LivingDropsEvent) forgeEvent;

        ImmutableList.Builder<EntitySnapshot> builder = new ImmutableList.Builder<>();
        for (EntityItem entityItem : drops) {
            builder.add(((Entity) entityItem).createSnapshot());
        }
        this.entitySnapshots = builder.build();
    }

    @Override
    public List<EntitySnapshot> getEntitySnapshots() {
        return this.entitySnapshots;
    }

    @Override
    public List<Entity> getEntities() {
        return (List<Entity>)(List<?>) this.drops;
    }

    @Override
    public List<Entity> filterEntityLocations(Predicate<Location<World>> predicate) {
        Iterator<Entity> iterator = ((List<Entity>)(List<?>) this.drops).iterator();
        while (iterator.hasNext()) {
            if (!predicate.test(iterator.next().getLocation())) {
                iterator.remove();
            }
        }
        return (List<Entity>)(List<?>) this.drops;
    }

    @Override
    public List<Entity> filterEntities(Predicate<Entity> predicate) {
        Iterator<Entity> iterator = ((List<Entity>)(List<?>) this.drops).iterator();
        while (iterator.hasNext()) {
            if (!predicate.test(iterator.next())) {
                iterator.remove();
            }
        }
        return (List<Entity>)(List<?>) this.drops;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public World getTargetWorld() {
        return (World) this.entity.worldObj;
    }
}
