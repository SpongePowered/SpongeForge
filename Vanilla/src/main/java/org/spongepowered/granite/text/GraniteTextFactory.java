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
package org.spongepowered.granite.text;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.TextFactory;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.granite.text.format.GraniteTextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NonnullByDefault
public class GraniteTextFactory implements TextFactory {

    @Override
    public Text parseJson(String json) throws IllegalArgumentException {
        try {
            return ((GraniteChatComponent) IChatComponent.Serializer.jsonToComponent(json)).toText();
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Failed to parse JSON", e);
        }
    }

    @Override
    public Text parseJsonLenient(String json) throws IllegalArgumentException {
        return parseJson(json); // TODO
    }

    @Override
    public String toPlain(Text text) {
        return ((GraniteText) text).toPlain();
    }

    @Override
    public String toJson(Text text) {
        return ((GraniteText) text).toJson();
    }

    @Override
    public char getLegacyChar() {
        return GraniteText.COLOR_CHAR;
    }

    private static final ImmutableMap<Character, EnumChatFormatting> CHAR_TO_FORMATTING;

    static {
        ImmutableMap.Builder<Character, EnumChatFormatting> builder = ImmutableMap.builder();

        for (EnumChatFormatting formatting : EnumChatFormatting.values()) {
            builder.put(formatting.formattingCode, formatting);
        }

        CHAR_TO_FORMATTING = builder.build();
    }

    private static void applyStyle(TextBuilder builder, char code) {
        EnumChatFormatting formatting = CHAR_TO_FORMATTING.get(code);
        if (formatting != null) {
            switch (formatting) {
                case BOLD:
                    builder.style(TextStyles.BOLD);
                    break;
                case ITALIC:
                    builder.style(TextStyles.ITALIC);
                    break;
                case UNDERLINE:
                    builder.style(TextStyles.UNDERLINE);
                    break;
                case STRIKETHROUGH:
                    builder.style(TextStyles.STRIKETHROUGH);
                    break;
                case OBFUSCATED:
                    builder.style(TextStyles.OBFUSCATED);
                    break;
                case RESET:
                    builder.color(TextColors.NONE);
                    builder.style(TextStyles.RESET);
                    break;
                default:
                    builder.color(GraniteTextColor.of(formatting));
            }
        }
    }

    private static Text.Literal parseLegacyMessage(String text, int pos, Matcher matcher, TextBuilder.Literal parent) {
        String content = text.substring(pos, matcher.start());
        parent.content(content);

        TextBuilder.Literal builder = Texts.builder("");
        applyStyle(builder, matcher.group(1).charAt(0));

        int end = matcher.end();
        while (true) {
            if (!matcher.find()) {
                builder.content(text.substring(end));
                return builder.build();
            } else if (end == matcher.start()) {
                applyStyle(builder, matcher.group(1).charAt(0));
                end = matcher.end();
            } else {
                break;
            }
        }

        builder.append(parseLegacyMessage(text, end, matcher, builder));
        return builder.build();
    }

    @Override
    public Text.Literal parseLegacyMessage(String text, char code) {
        if (text.length() <= 1)
            return Texts.of(text);

        Matcher matcher = (code == GraniteText.COLOR_CHAR ? FORMATTING_PATTERN :
                Pattern.compile(code + "([0-9A-FK-OR])", CASE_INSENSITIVE)).matcher(text);
        if (!matcher.find()) {
            return Texts.of(text);
        }

        return parseLegacyMessage(text, 0, matcher, Texts.builder(""));
    }

    private static final Pattern FORMATTING_PATTERN = Pattern.compile(GraniteText.COLOR_CHAR + "([0-9A-FK-OR])", CASE_INSENSITIVE);

    @Override
    public String stripLegacyCodes(String text, char code) {
        if (code == GraniteText.COLOR_CHAR) {
            return FORMATTING_PATTERN.matcher(text).replaceAll("");
        }

        return text.replaceAll("(?i)" + code + "[0-9A-FK-OR]", "");
    }

    @Override
    public String replaceLegacyCodes(String text, char from, char to) {
        if (from == GraniteText.COLOR_CHAR) {
            return FORMATTING_PATTERN.matcher(text).replaceAll(to + "$1");
        }

        return text.replaceAll("(?i)" + from + "([0-9A-FK-OR])", to + "$1");
    }

    @Override
    public String toLegacy(Text text, char code) {
        return ((GraniteText) text).toLegacy(code);
    }

}
