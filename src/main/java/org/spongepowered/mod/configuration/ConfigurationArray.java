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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ConfigurationArray<T> extends ConfigurationElement<T[]> implements ConfigArray<T> {

    private List<T> list = new ArrayList<T>();

    public ConfigurationArray(ConfigElement<?> parent) {
        super(parent);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public T getElement(int index) {
        return list.get(index);
    }

    @Override
    public void add(T object) {
        list.add(object);
    }

    @Override
    public void add(T object, int index) {
        list.add(index, object);
    }

    @Override
    public void addAll(T... objects) {
        list.addAll(Arrays.asList(objects));
    }

    @Override
    public void remove(T object) {
        list.remove(object);
    }

    @Override
    public void remove(int index) {
        list.remove(index);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean contains(T object) {
        return list.contains(object);
    }

    @Override
    public int getIndexOf(T object) {
        return list.indexOf(object);
    }

}
