package org.janus.lib.tools;

import java.util.Arrays;

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
}
