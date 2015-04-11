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
package org.spongepowered.mod.mixin.core.entity;

import com.google.common.base.Optional;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityItem.class)
public abstract class MixinEntityItem extends MixinEntity implements Item {

    private static final short MAGIC_INFINITE_PICKUP_DELAY = 32767;
    private static final short MAGIC_INFINITE_DESPAWN_TIME = -32768;
    private static final int MAGIC_INFINITE = -1;

    @Shadow private int delayBeforeCanPickup;
    @Shadow private int age;
    @Shadow(remap = false)
    public int lifespan;
    @Shadow public abstract net.minecraft.item.ItemStack getEntityItem();

    //
    // In the case where a Forge mod sets the delay to MAGIC_INFINITE_PICKUP_DELAY, but a plugin has
    // never called setPickupDelay or setInfinitePickupDelay, delayBeforeCanPickup would be decremented,
    // as infiniteDelay is set to false. However, this is not the intended behavior, as the Forge
    // mod meant an infinite delay to be set.

    // To resolve the ambiguity, this flag is used to determine whether infiniteDelay is false because it was never changed
    // from the default, or if it was explicitly set by a plugin
    private boolean pluginPickupSet;
    private boolean infinitePickupDelay;
    private boolean pluginDespawnSet;
    private boolean infiniteDespawnDelay;

    @Inject(method = "onUpdate()V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityItem;delayBeforeCanPickup:I",
            opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void onOnUpdate(CallbackInfo ci) {
        if (this.delayBeforeCanPickup == MAGIC_INFINITE_PICKUP_DELAY && !this.infinitePickupDelay && this.pluginPickupSet) {
            this.delayBeforeCanPickup--;
        }
    }

    @Inject(method = "onUpdate()V", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityItem;age:I", opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER))
    private void onOnUpdateAge(CallbackInfo ci) {
        if (this.delayBeforeCanPickup == MAGIC_INFINITE_DESPAWN_TIME && !this.infiniteDespawnDelay && this.pluginDespawnSet) {
            this.delayBeforeCanPickup--;
        }
    }

    public int getPickupDelay() {
        if (this.delayBeforeCanPickup == MAGIC_INFINITE_PICKUP_DELAY) {
            // There are two cases when -1 should be returned:

            // The plugin has called set an infinite pickup delay
            // The plugin has not set a pickup delay (neither setPickupDelay nor setInfinitePickupDelay
            // has been called) - a Forge mod or something else has set the pickup delay, and they presumably
            // know about the magic value.
            if ((this.pluginPickupSet && this.infinitePickupDelay) || !this.pluginPickupSet) {
                return MAGIC_INFINITE;
            }
        }
        return this.delayBeforeCanPickup;
    }

    public void setPickupDelay(int delay) {
        this.delayBeforeCanPickup = delay;
        this.pluginPickupSet = true;
        this.infinitePickupDelay = false;
    }

    public void setInfinitePickupDelay() {
        this.delayBeforeCanPickup = MAGIC_INFINITE_PICKUP_DELAY;
        this.pluginPickupSet = true;
        this.infinitePickupDelay = true;
    }

    public int getDespawnTime() {
        if (this.age == MAGIC_INFINITE_DESPAWN_TIME) {
            if ((this.pluginDespawnSet && this.infiniteDespawnDelay) || !this.pluginDespawnSet) {
                return MAGIC_INFINITE;
            }
        }
        return this.lifespan - this.age;
    }

    public void setDespawnTime(int time) {
        this.lifespan = this.age + time;
        this.pluginDespawnSet = true;
        this.infiniteDespawnDelay = false;
    }

    public void setInfiniteDespawnTime() {
        this.age = MAGIC_INFINITE_DESPAWN_TIME;
        this.pluginDespawnSet = true;
        this.infiniteDespawnDelay = true;
    }

    public ItemStack getItemStack() {
        return (ItemStack) getEntityItem();
    }

    // TODO: Implement getThrower once some class implements User
    public Optional<User> getThrower() {
        throw new UnsupportedOperationException();
    }

    public void setThrower(@Nullable User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        // If the key exists, the value has been set by a plugin
        if (compound.hasKey("infinitePickupDelay")) {
            this.pluginPickupSet = true;
            if (compound.getBoolean("infinitePickupDelay")) {
                this.setInfinitePickupDelay();
            } else {
                this.infinitePickupDelay = false;
            }
        }
        if (compound.hasKey("infiniteDespawnDelay")) {
            this.pluginDespawnSet = true;
            if (compound.getBoolean("infiniteDespawnDelay")) {
                this.setInfiniteDespawnTime();
            } else {
                this.infiniteDespawnDelay = false;
            }
        }
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        if (this.pluginPickupSet) {
            compound.setBoolean("infinitePickupDelay", this.infinitePickupDelay);
        } else {
            compound.removeTag("infinitePickupDelay");
        }
        if (this.pluginDespawnSet) {
            compound.setBoolean("infiniteDespawnDelay", this.infiniteDespawnDelay);
        } else {
            compound.removeTag("infiniteDespawnDelay");
        }
    }

}
