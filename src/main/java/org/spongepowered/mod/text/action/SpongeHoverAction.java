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
package org.spongepowered.mod.text.action;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class SpongeHoverAction<R> implements HoverAction<R> {

    private final String id;
    private final R result;

    public SpongeHoverAction(String id, R result) {
        this.id = id;
        this.result = result;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public R getResult() {
        return this.result;
    }

    public static class ShowText extends SpongeHoverAction<Message> implements HoverAction.ShowText {

        public ShowText(String id, Message result) {
            super(id, result);
        }

    }

    public static class ShowItem extends SpongeHoverAction<ItemStack> implements HoverAction.ShowItem {

        public ShowItem(String id, ItemStack result) {
            super(id, result);
        }

    }

    // TODO Replace Object with Achievement
    public static class ShowAchievement extends SpongeHoverAction<Object> implements HoverAction.ShowAchievement {

        public ShowAchievement(String id, Object result) {
            super(id, result);
        }

    }

    public static class ShowEntity extends SpongeHoverAction<Entity> implements HoverAction.ShowEntity {

        public ShowEntity(String id, Entity result) {
            super(id, result);
        }

    }
}
