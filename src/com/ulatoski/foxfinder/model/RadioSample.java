package com.ulatoski.foxfinder.model;

public class RadioSample {

    private String frequency = "";
    private int sMeter = 0;
    private boolean firstSample = true;

    /**
     * This constructor requires all fields
     *
     * @param frequency
     * contains 6 digits split in half by a decimal (###.###)
     * @param sMeter
     * value ranging from 0 to 12
     * @param firstSample
     * is true on the completion of an antenna rotation.
     */
    public RadioSample(String frequency, int sMeter, boolean firstSample) throws IllegalArgumentException {
        verify(frequency);
        verify(sMeter);
        this.frequency = frequency;
        this.sMeter = sMeter;
        this.firstSample = firstSample;
    }

    public String getFrequency() {
        return frequency;
    }

    public int getSMeter() {
        return sMeter;
    }

    public boolean isFirstSample() {
        return firstSample;
    }

    private void verify(String frequency) throws IllegalArgumentException {
        if (!frequency.matches("\\d{3}\\.\\d{3}")) throw new IllegalArgumentException("Frequency must be ###.###");
    }

    private void verify(int sMeter) throws IllegalArgumentException {
        if (sMeter < 0 || sMeter > 16) throw new IllegalArgumentException("Illegal S-Meter value.");
    }

}

