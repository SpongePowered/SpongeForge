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
package org.spongepowered.mod.mixin.core.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.api.entity.ai.task.AITask;
import org.spongepowered.api.entity.ai.task.AITaskType;
import org.spongepowered.api.entity.ai.task.AbstractAITask;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.ai.IMixinEntityAIBase;
import org.spongepowered.common.registry.type.entity.AITaskTypeModule;

import java.util.Optional;

@Mixin(value = EntityAIBase.class, priority = 1001)
public abstract class MixinEntityAIBase {

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void addModAIType(CallbackInfo ci) {
        // Only set a type if we have none
        if (((AITask<Agent>) this).getType() != null) {
            return;
        }
        // API custom tasks handle types differently, ignore
        if (AbstractAITask.class.isAssignableFrom(getClass())) {
            return;
        }
        // Handle adding mod/un-implemented Minecraft tasks.
        final Optional<AITaskType> optModType = AITaskTypeModule.getInstance().getByAIClass(getClass());
        if (!optModType.isPresent()) {
            PluginContainer container = (PluginContainer) Loader.instance().activeModContainer();
            // FML couldn't figure out the mod...give the task to Minecraft
            if (container == null) {
                // May need to log this...
                container = SpongeImpl.getMinecraftPlugin();
            }
            final String idAndName = getClass().getSimpleName();
            ((IMixinEntityAIBase) this).setType(AITaskTypeModule.getInstance().createAITaskType(container, idAndName, idAndName,
                    (Class<? extends AITask<? extends Agent>>) getClass()));
        }
    }
}
