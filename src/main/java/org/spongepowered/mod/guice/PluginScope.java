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

package org.spongepowered.mod.guice;

import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Map;

public class PluginScope implements Scope {
    Map<PluginContainer, Map<Key<?>, Object>> scopes = Maps.newHashMap();
    PluginContainer current_scope;

    public void setScope(PluginContainer scope) {
        current_scope = scope;
        if(current_scope != null && !scopes.containsKey(current_scope)) {
            Map<Key<?>, Object> initial_scope = Maps.newHashMap();
            initial_scope.put(Key.get(PluginContainer.class), current_scope);
            scopes.put(current_scope, initial_scope);
        }
    }

    public <T> void setInstance(Key<T> key, T instance) {
        if(current_scope == null) {
            throw new OutOfScopeException("No current plugin scope defined");
        }

        Map<Key<?>, Object> scope = scopes.get(current_scope);
        scope.put(key, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Key<T> key) {
        if(current_scope == null) {
            throw new OutOfScopeException("No current plugin scope defined");
        }

        Map<Key<?>, Object> scope = scopes.get(current_scope);
        return (T)scope.get(key);
    }

    @Override
    public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
        return new PluginScopedProvider<T>(key, unscoped);
    }

    @Override
    public String toString() {
        return "SpongeScopes.PLUGIN";
    }

    private class PluginScopedProvider<T> implements Provider<T> {
        private Provider<T> unscoped;
        private Key<T> key;

        public PluginScopedProvider(Key<T> key, Provider<T> base) {
            this.key = key;
            unscoped = base;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            if(current_scope == null) {
                throw new OutOfScopeException("No current plugin scope defined");
            }

            Map<Key<?>, Object> scope = scopes.get(current_scope);

            if(scope.containsKey(key)) {
                return (T)scope.get(key);
            } else {
                T instance = unscoped.get();
                scope.put(key, instance);
                return instance;
            }
        }

        @Override
        public String toString() {
            return "PluginScopedProvider " + unscoped.toString();
        }
    }
}
