package org.janelia.scicomp.lib.tools;

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
