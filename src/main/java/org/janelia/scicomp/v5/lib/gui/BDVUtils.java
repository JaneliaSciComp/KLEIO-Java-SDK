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

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Bdv;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.histogram.DiscreteFrequencyDistribution;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.histogram.Real1dBinMapper;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import spim.fiji.spimdata.explorer.util.ColorStream;

import java.util.Iterator;
import java.util.List;

/**
 * Code copied from BigStitcher
 * net.preibisch.mvrecon.fiji.spimdata.explorer.popup.BDVPopup
 **/
public class BDVUtils {
    public static <T extends NumericType<T> & NativeType<T>> void randomColor(Bdv bdv) {
        bdv.getBdvHandle().getViewerPanel().showMessage("Next Random Color");
        Iterator<ARGBType> iterator = ColorStream.iterator();
        List<ConverterSetup> converters = bdv.getBdvHandle().getSetupAssignments().getConverterSetups();
        for (int i = 0; i < converters.size(); ++i)
            converters.get(i).setColor(iterator.next());
        bdv.getBdvHandle().getViewerPanel().requestRepaint();
    }

}
