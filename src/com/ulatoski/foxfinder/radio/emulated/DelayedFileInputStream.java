package com.ulatoski.foxfinder.radio.emulated;

import java.io.*;

/**
 * Created by tojified on 3/2/14.
 */
public class DelayedFileInputStream extends FileInputStream {

    static int INDEX_REFRESH = 150;

    long delay = 1;
    int indexCounter = 0;

    public DelayedFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }

    public DelayedFileInputStream(FileDescriptor fd) {
        super(fd);
    }

    public DelayedFileInputStream(String path) throws FileNotFoundException {
        super(path);
    }

    public void setDelay(long timeMs) {
        delay = timeMs;
    }

    @Override
    public long skip(long byteCount) throws IOException {
        delay();
        return super.skip(byteCount);
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        delay();
        return super.read(buffer);
    }

    @Override
    public int read() throws IOException {
        delay();
        int b = super.read();
        if (b == 0xFF) {
            indexCounter++;
            if (indexCounter > INDEX_REFRESH) {
                indexCounter = 0;
                b = 0xF0;
            }
        }
        return b;
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        delay();
        return super.read(buffer, byteOffset, byteCount);
    }

    private void delay() {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
