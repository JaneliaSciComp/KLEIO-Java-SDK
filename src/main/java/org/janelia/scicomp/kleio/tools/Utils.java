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

package org.janelia.scicomp.kleio.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static String format(double[][] list) {
        StringBuilder result = new StringBuilder("(");
        for (int i = 0; i < list.length; i++) {
            result.append(Arrays.toString(list[i]));
            if (i < list.length - 1)
                result.append("-");
        }
        result.append(")");
        return result.toString();
    }


    public static String format(List<long[]> list) {
        String result = "";
        int i ;
        for (i = 0;i<list.size()-1;++i){
            result =  result+format(list.get(i))+"_";

        }
        result = result+format(list.get(i));
        return result;
    }

    public static String format(long[] l) {
        String result = "";
        int i ;
        for ( i = 0; i < l.length-1; ++i) {
            result = result+l[i]+"-";
        }
        result = result+l[i];
        return result;
    }


    public static void main(String[] args) {
        long[] l = new long[]{2,3,4};
        List<long[]> all = new ArrayList<>();
        all.add(l);
        all.add(l);
        System.out.println(format(all));
    }
}
