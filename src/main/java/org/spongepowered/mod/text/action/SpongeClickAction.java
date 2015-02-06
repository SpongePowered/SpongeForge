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

import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.net.URL;

@NonnullByDefault
public class SpongeClickAction<R> implements ClickAction<R> {

    private final String id;
    private final R result;

    public SpongeClickAction(String id, R result) {
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

    public static class OpenUrl extends SpongeClickAction<URL> implements ClickAction.OpenUrl {

        public OpenUrl(String id, URL result) {
            super(id, result);
        }

    }

    public static class RunCommand extends SpongeClickAction<String> implements ClickAction.RunCommand {

        public RunCommand(String id, String result) {
            super(id, result);
        }

    }

    public static class ChangePage extends SpongeClickAction<Integer> implements ClickAction.ChangePage {

        public ChangePage(String id, Integer result) {
            super(id, result);
        }

    }

    public static class SuggestCommand extends SpongeClickAction<String> implements ClickAction.SuggestCommand {

        public SuggestCommand(String id, String result) {
            super(id, result);
        }

    }
}
