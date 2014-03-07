package com.ulatoski.foxfinder.radio;

import com.ulatoski.foxfinder.model.RadioButton;
import com.ulatoski.foxfinder.model.RadioSample;

import java.io.*;

public class RadioOutputStream extends DataOutputStream {

    private OutputStream stream;

    public RadioOutputStream(OutputStream output) throws StreamCorruptedException, IOException {
        super(output);
        stream = output;
    }

    public void writeRadioSample(RadioSample radioSample) throws IOException {
        String freq = radioSample.getFrequency().replaceFirst("", "");
        int sMeter = radioSample.getSMeter();

        stream.write(0x10); stream.write(radioSample.isFirstSample() ? 0xF0 : 0xFF);

        stream.write(0x20); stream.write(Integer.valueOf(freq.substring(0,1), 16));
        stream.write(0x30); stream.write(Integer.valueOf(freq.substring(2,3), 16));
        stream.write(0x40); stream.write(Integer.valueOf(freq.substring(4,5), 16));

        stream.write(0x58); stream.write(0x58);

        stream.write(0x60); stream.write(sMeter == 0 ? 0 : sMeter + 0x40);

        stream.write(0);
        stream.write(0);
        stream.write(0);

    }

    public void writeRadioButton(RadioButton button) throws IOException {
        stream.write(button.getValue());
    }

}
