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

import java.util.List;
import java.util.stream.Collectors;

public class LabelMultisetTypeConverter implements Converter<LabelMultisetType, UnsignedShortType> {

    private final Label entry;

    public LabelMultisetTypeConverter(LabelMultisetType entry) {

        if (entry.entrySet().size() != 1) throw new RuntimeException("Works only for one label !");

        this.entry  = entry.entrySet().iterator().next().getElement();
    }


    @Override
    public void convert(LabelMultisetType input, UnsignedShortType output) {
        if (!input.contains(entry)) output.set(1);
        else output.set(0);
    }

    public static final RandomAccessibleInterval<UnsignedShortType> convertVirtual(final RandomAccessibleInterval<LabelMultisetType> img) {
        LabelMultisetType entry = getLabelEntry(img);
        return new ConvertedRandomAccessibleInterval<LabelMultisetType, UnsignedShortType>(img, new LabelMultisetTypeConverter(entry), new UnsignedShortType());
    }

    private static final LabelMultisetType getLabelEntry(RandomAccessibleInterval<LabelMultisetType> img) {

        Cursor<LabelMultisetType> cursor = Views.iterable(img).localizingCursor();

        cursor.fwd();
        return cursor.get();
    }

}
