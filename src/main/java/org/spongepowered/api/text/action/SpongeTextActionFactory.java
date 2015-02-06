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
package org.spongepowered.api.text.action;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.action.ClickAction.ChangePage;
import org.spongepowered.api.text.action.ClickAction.OpenUrl;
import org.spongepowered.api.text.action.ClickAction.RunCommand;
import org.spongepowered.api.text.action.ClickAction.SuggestCommand;
import org.spongepowered.api.text.action.HoverAction.ShowAchievement;
import org.spongepowered.api.text.action.HoverAction.ShowEntity;
import org.spongepowered.api.text.action.HoverAction.ShowItem;
import org.spongepowered.api.text.action.HoverAction.ShowText;
import org.spongepowered.api.text.action.ShiftClickAction.InsertText;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.mod.text.action.SpongeClickAction;
import org.spongepowered.mod.text.action.SpongeHoverAction;
import org.spongepowered.mod.text.action.SpongeShiftClickAction;

import java.net.URL;

@NonnullByDefault
public class SpongeTextActionFactory implements TextActionFactory {

    @Override
    public OpenUrl createOpenUrl(URL url) {
        return new SpongeClickAction.OpenUrl("", url);
    }

    @Override
    public RunCommand createRunCommand(String command) {
        return new SpongeClickAction.RunCommand("", command);
    }

    @Override
    public ChangePage createChangePage(int page) {
        return new SpongeClickAction.ChangePage("", Integer.valueOf(page));
    }

    @Override
    public SuggestCommand createSuggestCommand(String command) {
        return new SpongeClickAction.SuggestCommand("", command);
    }

    @Override
    public ShowText createShowText(Message message) {
        return new SpongeHoverAction.ShowText("", message);
    }

    @Override
    public ShowItem createShowItem(ItemStack item) {
        return new SpongeHoverAction.ShowItem("", item);
    }

    @Override
    public ShowAchievement createShowAchievement(Object achievement) {
        return new SpongeHoverAction.ShowAchievement("", achievement);
    }

    @Override
    public ShowEntity createShowEntity(Entity entity) {
        return new SpongeHoverAction.ShowEntity("", entity);
    }

    @Override
    public InsertText createInsertText(String text) {
        return new SpongeShiftClickAction.InsertText("", text);
    }

}
