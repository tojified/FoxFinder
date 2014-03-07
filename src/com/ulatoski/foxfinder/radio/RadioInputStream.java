package com.ulatoski.foxfinder.radio;

import java.io.*;

import android.util.Log;
import com.ulatoski.foxfinder.model.RadioSample;

public class RadioInputStream extends DataInputStream {

    private InputStream stream;

    public RadioInputStream(InputStream input) throws StreamCorruptedException, IOException {
        super(input);
        stream = input;
    }

    public RadioSample readRadioSample() throws IOException {
        int b;
        while( (b = stream.read()) != -1 ) {  // until EOF

            // byte 01 : 0x10
            if (b != 0x10) continue;

            // byte 02 : 0xF0 or 0xFF
            int indexByte = stream.read();
            if (!validIndex(indexByte)) continue;

            // byte 03-08 : 0x20 0x## 0x30 0x## 0x40 0x## = (###.###)
            StringBuilder freq = new StringBuilder();
            for (int i = 2; i <= 4; i++) {
                if (stream.read() != i * 0x10) continue; // 0x20, 0x30, 0x40
                freq.append(String.format("%02X", stream.read()));
            }
            if (freq.length() != 6) continue;

            freq.insert(3, '.'); // insert decimal (i.e. 146.415)

            // byte 09-10 : ignored
            if (stream.skip(2) < 2) continue;

            // byte 11
            if (stream.read() != 0x60) continue;

            // byte 12
            int sMtr = stream.read();
            if (sMtr > 0x40) sMtr = sMtr - 0x40;

            try {
                return new RadioSample(freq.toString(), sMtr, indexByte == 0xF0);
            } catch (IllegalArgumentException ex) {
                Log.e("RadioSample", "Bad data received! Freq:" + freq.toString() + ", sMtr:" + sMtr);
            }
        }
        return null;
    }

    private boolean validIndex(int b) { return b == 0xFF || b == 0xF0; }

}
