/**
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
package org.spongepowered.mod.configuration;

import org.spongepowered.api.configuration.ConfigArray;
import org.spongepowered.api.configuration.ConfigElement;
import org.spongepowered.api.configuration.ConfigNull;
import org.spongepowered.api.configuration.ConfigObject;
import org.spongepowered.api.configuration.ConfigPrimitive;

public abstract class ConfigurationElement<T> implements ConfigElement<T> {

    private ConfigElement<?> parent;
    private String comment;
    private boolean save = true;
    protected boolean used = false;

    public ConfigurationElement(ConfigElement<?> parent) {
        this.parent = parent;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public void delete() {
        this.save = false;
    }

    public boolean willSave() {
        return this.save;
    }

    public ConfigElement<?> getParent() {
        return this.parent;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigArray<T> getAsArray() {
        if (this instanceof ConfigArray) {
            return (ConfigArray<T>) this;
        }
        throw new ClassCastException();
    }

    @Override
    public ConfigObject getAsObject() {
        if (this instanceof ConfigObject) {
            return (ConfigObject) this;
        }
        throw new ClassCastException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigPrimitive<T> getAsPrimitive() {
        if (this instanceof ConfigPrimitive) {
            return (ConfigPrimitive<T>) this;
        }
        throw new ClassCastException();
    }

    @Override
    public ConfigNull getAsNull() {
        if (this instanceof ConfigNull) {
            return (ConfigNull) this;
        }
        throw new ClassCastException();
    }

}
