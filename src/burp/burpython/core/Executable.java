package burp.burpython.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public interface Executable {
    public void handle(BufferedReader br, BufferedWriter bw) throws IOException;
    public void fail();
}