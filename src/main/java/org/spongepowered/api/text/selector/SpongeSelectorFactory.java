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
package org.spongepowered.api.text.selector;

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.text.selector.SpongeSelectorBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NonnullByDefault
public class SpongeSelectorFactory implements SelectorFactory {

    private static final Pattern tokenPattern = Pattern.compile("^@([pare])(?:\\[([\\w=,!-]*)\\])?$");
    private static final Pattern shortcutMatcher = Pattern.compile("\\G([-!]?[\\w-]*)(?:$|,)");
    private static final Pattern mapPattern = Pattern.compile("\\G(\\w+)=([-!]?[\\w-]*)(?:$|,)");

    @Override
    public SelectorBuilder createEmptyBuilder() {
        return new SpongeSelectorBuilder();
    }

    @Override
    public Selector parseRawSelector(String selector) {
        checkArgument(tokenPattern.matcher(selector).matches());
        SelectorBuilder builder = createEmptyBuilder().selectorType(SelectorTypes.valueOf(selector.substring(1, 2)).get());
        setArguments(builder, selector.substring(selector.indexOf('[') + 1, selector.lastIndexOf(']')));
        return builder.build();
    }

    private void setArguments(SelectorBuilder builder, String argString) {
        if (argString != null) {
            int i = 0;
            int j = -1;

            for (Matcher shortcuts = shortcutMatcher.matcher(argString); shortcuts.find(); j = shortcuts.end()) {
                String key = null;

                switch (i++) {
                    case 0:
                        key = "x";
                        break;
                    case 1:
                        key = "y";
                        break;
                    case 2:
                        key = "z";
                        break;
                    case 3:
                        key = "r";
                }

                if (key != null && shortcuts.group(1).length() > 0) {
                    builder.addArgument(key, shortcuts.group(1));
                }
            }

            if (j < argString.length()) {
                Matcher entries = mapPattern.matcher(j == -1 ? argString : argString.substring(j));

                while (entries.find()) {
                    builder.addArgument(entries.group(1), entries.group(2));
                }
            }
        }
    }
}
