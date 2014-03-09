package com.ulatoski.foxfinder.radio.emulated;

import java.io.*;
import java.net.Socket;

/**
 * Created by tojified on 3/1/14. \
 * Emulates Radio data provided by bluetooth adapter by providing input and output streams
 */
public class BluetoothSocket extends Socket {

    DelayedFileInputStream sampleDataStream = null;

    public BluetoothSocket(File directory, String filename) throws IOException {
        File dataFile = new File(directory, filename);
        connect(dataFile, 3);
    }

    public void close() throws IOException {
        sampleDataStream.close();
        sampleDataStream = null;
    }

    public void connect(File dataFile, int delayInMs) throws IOException {
        try {
            sampleDataStream = new DelayedFileInputStream(dataFile);
            sampleDataStream.setDelay(delayInMs);
        } catch (FileNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    public InputStream getInputStream() {
        return sampleDataStream;
    }

    public OutputStream getOutputStream() {
        return System.out;
    }

    public boolean isConnected() {
        return sampleDataStream != null;
    }


}
