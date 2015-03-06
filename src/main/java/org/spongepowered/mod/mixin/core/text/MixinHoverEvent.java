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
package org.spongepowered.mod.mixin.core.text;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Throwables;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.SpongeMod;
import org.spongepowered.mod.text.SpongeChatComponent;
import org.spongepowered.mod.text.SpongeHoverEvent;

import java.util.UUID;

@Mixin(HoverEvent.class)
public abstract class MixinHoverEvent implements SpongeHoverEvent {

    @Shadow private HoverEvent.Action action;
    @Shadow private IChatComponent value;

    private HoverAction<?> handle;
    private boolean initialized;

    @Override
    public HoverAction<?> getHandle() {
        if (!this.initialized) {
            try {
                // This is inefficient, but at least we only need to do it once
                switch (this.action) {
                    case SHOW_TEXT:
                        setHandle(TextActions.showText(((SpongeChatComponent) this.value).toText()));
                        break;
                    case SHOW_ACHIEVEMENT:
                        String stat = this.value.getUnformattedText();
                        setHandle(TextActions.showAchievement(checkNotNull(StatList.getOneShotStat(stat), "Unknown statistic: %s", stat)));
                        break;
                    case SHOW_ITEM:
                        setHandle(TextActions.showItem((ItemStack) net.minecraft.item.ItemStack.loadItemStackFromNBT(loadNbt())));
                        break;
                    case SHOW_ENTITY:
                        NBTTagCompound nbt = loadNbt();
                        String name = nbt.getString("name");
                        EntityType type = null;
                        if (nbt.hasKey("type", 8)) {
                            type = SpongeMod.instance.getGame().getRegistry().getEntity(name).orNull();
                        }

                        UUID uniqueId = UUID.fromString(nbt.getString("id"));
                        setHandle(TextActions.showEntity(uniqueId, name, type));
                        break;
                    default:
                }
            } finally {
                this.initialized = true;
            }
        }

        return this.handle;
    }

    private NBTTagCompound loadNbt() {
        try {
            return checkNotNull(JsonToNBT.getTagFromJson(this.value.getUnformattedText()), "NBT");
        } catch (NBTException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void setHandle(HoverAction<?> handle) {
        if (this.initialized) {
            return;
        }

        this.handle = checkNotNull(handle, "handle");
        this.initialized = true;
    }

}
