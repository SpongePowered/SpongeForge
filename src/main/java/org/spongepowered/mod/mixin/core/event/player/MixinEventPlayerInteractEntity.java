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
package org.spongepowered.mod.mixin.core.event.player;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.mod.mixin.core.event.entity.MixinEventEntity;

import java.util.Optional;

@NonnullByDefault
@Mixin(value = EntityInteractEvent.class, remap = false)
public abstract class MixinEventPlayerInteractEntity extends MixinEventEntity implements InteractEntityEvent.Secondary {

    @Shadow @Final Entity target;

    private Cause cause;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(CallbackInfo callbackInfo) {
        this.cause = Cause.of(NamedCause.source(((EntityEvent) (Object) this).entity));
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public org.spongepowered.api.entity.Entity getTargetEntity() {
        return (org.spongepowered.api.entity.Entity) this.target;
    }

    @Override
    public Optional<Vector3d> getInteractionPoint() {
        return Optional.empty(); // TODO
    }
}
