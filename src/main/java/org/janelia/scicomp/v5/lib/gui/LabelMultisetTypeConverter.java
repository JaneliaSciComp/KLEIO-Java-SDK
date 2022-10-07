/*
 * *
 *  * Copyright (c) 2022, Janelia
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * 1. Redistributions of source code must retain the above copyright notice,
 *  *    this list of conditions and the following disclaimer.
 *  * 2. Redistributions in binary form must reproduce the above copyright notice,
 *  *    this list of conditions and the following disclaimer in the documentation
 *  *    and/or other materials provided with the distribution.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.janelia.scicomp.v5.lib.gui;


import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.type.label.Label;
import net.imglib2.type.label.LabelMultisetType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;
import org.janelia.saalfeldlab.n5.N5Reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class LabelMultisetTypeConverter implements Converter<LabelMultisetType, UnsignedShortType> {
    public static final String IS_LABEL_MULTISET_KEY = "isLabelMultiset";
    private final Label entry;
    private final boolean isOneValue;
    private final List<Label> entries;

    public LabelMultisetTypeConverter(LabelMultisetType entry) {
        if (entry.entrySet().isEmpty())
            throw new RuntimeException("ERROR Empty LabelMultisetType !");
        if (entry.entrySet().size() == 1) {
            this.entry = entry.entrySet().iterator().next().getElement();
            this.isOneValue = true;
            this.entries = null;
        } else {
            this.entry = null;
            this.entries = getListed(entry);
            this.isOneValue = false;
        }
    }

    private List<Label> getListed(LabelMultisetType entrySet) {
        List<Label> result = new ArrayList<>();
        Iterator<LabelMultisetType.Entry<Label>> iterator = entrySet.entrySet().iterator();
        while (iterator.hasNext()) {
            result.add(iterator.next().getElement());
        }
        return result;
    }


    @Override
    public void convert(LabelMultisetType input, UnsignedShortType output) {
        if (isOneValue) {
            if (!input.contains(entry)) {
                output.set(1);
                return;
            }
        } else {
            for (Label e : entries)
                if (input.contains(e)) {
                    output.set(entries.indexOf(e) + 1);
                    return;
                }
        }
        output.set(0);
    }

    public static final RandomAccessibleInterval<UnsignedShortType> convertVirtual(final RandomAccessibleInterval<LabelMultisetType> img) {
        LabelMultisetType entry = getLabelEntry(img);
        return new ConvertedRandomAccessibleInterval<>(img, new LabelMultisetTypeConverter(entry), new UnsignedShortType());
    }

    private static final LabelMultisetType getLabelEntry(RandomAccessibleInterval<LabelMultisetType> img) {
        Cursor<LabelMultisetType> cursor = Views.iterable(img).localizingCursor();
        cursor.fwd();
        return cursor.get();
    }

    public static boolean isLabelMultisetType(final N5Reader n5, final String group) throws IOException {
        return Optional.ofNullable(n5.getAttribute(group, IS_LABEL_MULTISET_KEY, Boolean.class)).orElse(false);
    }
}
