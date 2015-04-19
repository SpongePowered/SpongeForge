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
package org.spongepowered.mod.text;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.util.IChatComponent;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

public class ChatComponentIterator extends UnmodifiableIterator<IChatComponent> {

    private SpongeChatComponent component;
    private Iterator<IChatComponent> children;
    @Nullable private Iterator<IChatComponent> currentChildIterator;

    public ChatComponentIterator(SpongeChatComponent component) {
        this.component = checkNotNull(component, "component");
    }

    public ChatComponentIterator(Iterator<IChatComponent> children) {
        this.children = checkNotNull(children, "children");
    }

    @Override
    public boolean hasNext() {
        return this.component != null || (this.currentChildIterator != null && this.currentChildIterator.hasNext()) || this.children.hasNext();
    }

    @Override
    @SuppressWarnings("unchecked")
    public IChatComponent next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (this.component != null) {
            this.children = this.component.childrenIterator();

            IChatComponent result = this.component;
            this.component = null;
            return result;
        } else if (this.currentChildIterator == null || !this.currentChildIterator.hasNext()) {
            this.currentChildIterator = ((SpongeChatComponent) this.children.next()).withChildren().iterator();
        }

        return this.currentChildIterator.next();
    }

}
