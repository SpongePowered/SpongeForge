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
package org.spongepowered.mod.service.persistence.builders.block.tile;

import com.google.common.base.Optional;
import net.minecraft.tileentity.TileEntityNote;
import org.spongepowered.api.Game;
import org.spongepowered.api.block.tile.Note;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.types.NotePitch;
import org.spongepowered.api.service.persistence.InvalidDataException;

import java.util.List;

public class SpongeNoteBuilder extends AbstractTileBuilder<Note> {

    public SpongeNoteBuilder(Game game) {
        super(game);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Note> build(DataView container) throws InvalidDataException {
        Optional<Note> noteOptional = super.build(container);
        if (!noteOptional.isPresent()) {
            throw new InvalidDataException("The container had insufficient data to create a Note tile entity!");
        }
        if (!container.contains(new DataQuery("Note"))) {
            throw new InvalidDataException("The container had insufficient data to create a Note tile entity!");
        }
        Note note = noteOptional.get();
//        NotePitch pitch = ((List<NotePitch>) this.game.getRegistry().getNotePitches()).get(container.getInt(new DataQuery("Note")).get());
        // TODO Write NoteData
//        note.setNoteData(pitch);
        ((TileEntityNote) note).validate();
        return Optional.of(note);
    }
}
