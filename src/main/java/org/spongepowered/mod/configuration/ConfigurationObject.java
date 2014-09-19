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

import com.google.common.collect.Maps;

import org.spongepowered.api.configuration.ConfigArray;
import org.spongepowered.api.configuration.ConfigElement;
import org.spongepowered.api.configuration.ConfigObject;
import org.spongepowered.api.configuration.ConfigPrimitive;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConfigurationObject extends ConfigurationElement<Object> implements ConfigObject {

    private Map<String, ConfigElement<? extends Object>> object = Maps
            .newHashMap();

    public ConfigurationObject(ConfigElement<?> parent) {
        super(parent);
    }

    @Override
    public Iterator<Entry<String, ConfigElement<? extends Object>>> iterator() {
        return object.entrySet().iterator();
    }

    @Override
    public ConfigElement<?> getElement(String string) {
        return object.get(string);
    }

    @Override
    public <T> ConfigElement<T> getElement(String key, Class<T> type) {
        return (ConfigElement<T>) object.get(key);
    }

    @Override
    public <T> ConfigElement<T> getElement(String key, T defaultValue) {
        if (!object.containsKey(key)) {
            ConfigElement value = null;
            if (defaultValue == null) {
                value = new ConfigurationNull(this);
            } else if (defaultValue instanceof ConfigElement) {
                throw new UnsupportedOperationException(
                        "Can't use a default value for non-primitives.");
            } else {
                value = new ConfigurationPrimitive(this, defaultValue);
            }
            object.put(key, value);
        }
        return (ConfigElement<T>) object.get(key);
    }

    @Override
    public <T> ConfigElement<T> getElement(String key, T defaultValue,
            String comment) {
        ConfigElement<T> value = getElement(key, defaultValue);
        if (value.getComment() == null || value.getComment().isEmpty()) {
            value.setComment(comment);
        }
        return value;
    }

    @Override
    public ConfigObject getObject(String key) {
        if (!object.containsKey(key)) {
            object.put(key, new ConfigurationObject(this));
        }
        return (ConfigObject) this.object.get(key);
    }

    @Override
    public ConfigObject getObject(String key, String comment) {
        if (!object.containsKey(key)) {
            object.put(key, new ConfigurationObject(this));
            object.get(key).setComment(comment);
        }
        return (ConfigObject) object.get(key);
    }

    @Override
    public <T> ConfigArray<T> getArray(String key, Class<T> type) {
        if (!object.containsKey(key)) {
            object.put(key, new ConfigurationArray<T>(this));
        }
        return (ConfigArray<T>) object.get(key);
    }

    @Override
    public <T> ConfigArray<T> getArray(String key, Class<T> type, String comment) {
        if (!object.containsKey(key)) {
            object.put(key, new ConfigurationArray<T>(this));
            object.get(key).setComment(comment);
        }
        return (ConfigArray<T>) object.get(key);
    }

    @Override
    public <T> ConfigPrimitive<T> getPrimitive(String key, Class<T> type) {
        if (!object.containsKey(key)) {
            object.put(key, new ConfigurationPrimitive<T>(this));
        }
        return (ConfigPrimitive<T>) object.get(key);
    }

    @Override
    public <T> ConfigPrimitive<T> getPrimitive(String key, T defaultValue) {
        if (!object.containsKey(key)) {
            object.put(key, new ConfigurationPrimitive<T>(this, defaultValue));
        }
        return (ConfigPrimitive<T>) object.get(key);
    }

    @Override
    public <T> ConfigPrimitive<T> getPrimitive(String key, T defaultValue,
            String comment) {
        if (!object.containsKey(key)) {
            ConfigPrimitive<T> prim = new ConfigurationPrimitive<T>(this, defaultValue);
            prim.setComment(comment);
            object.put(key, prim);
        }
        return (ConfigPrimitive<T>) object.get(key);
    }

    @Override
    public boolean hasElement(String key) {
        return object.containsKey(key);
    }

    @Override
    public Class<?> getElementType(String key) {
        return object.get(key).getClass();
    }

    @Override
    public void cleanup() {
        throw new UnsupportedOperationException();
    }

    protected Map<String, ConfigElement<? extends Object>> getObjectMap() {
        return this.object;
    }
}
