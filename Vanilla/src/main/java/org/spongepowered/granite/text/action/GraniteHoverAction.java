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
package org.spongepowered.granite.text.action;

import net.minecraft.entity.EntityList;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.granite.entity.GraniteEntityType;
import org.spongepowered.granite.text.GraniteText;

public class GraniteHoverAction {

    private GraniteHoverAction() {
    }

    private static HoverEvent.Action getType(HoverAction<?> action) {
        if (action instanceof HoverAction.ShowAchievement) {
            return HoverEvent.Action.SHOW_ACHIEVEMENT;
        } else if (action instanceof HoverAction.ShowEntity) {
            return HoverEvent.Action.SHOW_ENTITY;
        } else if (action instanceof HoverAction.ShowItem) {
            return HoverEvent.Action.SHOW_ITEM;
        } else if (action instanceof HoverAction.ShowText) {
            return HoverEvent.Action.SHOW_TEXT;
        }

        throw new UnsupportedOperationException(action.getClass().toString());
    }

    public static HoverEvent getHandle(HoverAction<?> action) {
        HoverEvent.Action type = getType(action);
        IChatComponent component;

        switch (type) {
            case SHOW_ACHIEVEMENT:
                component = new ChatComponentText(((StatBase) action.getResult()).statId);
                break;
            case SHOW_ENTITY: {
                HoverAction.ShowEntity.Ref entity = ((HoverAction.ShowEntity) action).getResult();

                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setString("id", entity.getUniqueId().toString());

                if (entity.getType().isPresent()) {
                    nbt.setString("type", EntityList.getStringFromID(((GraniteEntityType) entity.getType().get()).entityTypeId));
                }

                nbt.setString("name", entity.getName());
                component = new ChatComponentText(nbt.toString());
                break;
            }
            case SHOW_ITEM: {
                net.minecraft.item.ItemStack item = (net.minecraft.item.ItemStack) action.getResult();
                NBTTagCompound nbt = new NBTTagCompound();
                item.writeToNBT(nbt);
                component = new ChatComponentText(nbt.toString());
                break;
            }
            case SHOW_TEXT:
                component = ((GraniteText) action.getResult()).toComponent();
                break;
            default:
                throw new AssertionError();
        }

        return new HoverEvent(type, component);
    }

}
