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

import net.minecraft.event.ClickEvent;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.text.SpongeClickEvent;

import java.net.MalformedURLException;
import java.net.URL;

@Mixin(ClickEvent.class)
public abstract class MixinClickEvent implements SpongeClickEvent {

    @Shadow private ClickEvent.Action action;
    @Shadow private String value;

    private ClickAction<?> handle;
    private boolean initialized;

    @Override
    public ClickAction<?> getHandle() {
        if (!this.initialized) {
            try {
                switch (this.action) {
                    case OPEN_URL:
                        try {
                            setHandle(TextActions.openUrl(new URL(this.value)));
                        } catch (MalformedURLException e) {
                            throw new IllegalArgumentException("Invalid URL: " + this.value, e);
                        }
                        break;
                    case RUN_COMMAND:
                        setHandle(TextActions.runCommand(this.value));
                        break;
                    case SUGGEST_COMMAND:
                        setHandle(TextActions.suggestCommand(this.value));
                        break;
                    case CHANGE_PAGE:
                        setHandle(TextActions.changePage(Integer.parseInt(this.value)));
                        break;
                    default:
                }
            } finally {
                this.initialized = true;
            }
        }

        return this.handle;
    }

    @Override
    public void setHandle(ClickAction<?> handle) {
        if (this.initialized) {
            return;
        }

        this.handle = checkNotNull(handle, "handle");
        this.initialized = true;
    }

}
