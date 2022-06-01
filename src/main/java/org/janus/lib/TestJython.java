package org.janus.lib;

import org.python.util.PythonInterpreter;

public class TestJython {
    public static void main(String[] args) {
        try(PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.exec("print('Hello Python World!')");
        }
    }
}
