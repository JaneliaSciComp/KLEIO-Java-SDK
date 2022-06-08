package org.janus.lib;


import com.sun.jna.Library;
import com.sun.jna.Native;

public class CLibrary implements Library {
    static {
        Native.register("c");
    }


}

