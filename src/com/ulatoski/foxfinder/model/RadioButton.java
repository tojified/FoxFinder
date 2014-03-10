package com.ulatoski.foxfinder.model;

/**
 * Created by George on 3/6/14.
 */
public class RadioButton {

    public static final int NONE = 0;
    public static final int CALL = 2;
    public static final int VFO  = 4;
    public static final int MR   = 8;
    public static final int UP   = 16;
    public static final int DOWN = 32;
    public static final int FUNC = 64;
    public static final int PWR  = 128; //??? or 1?

    private int buttonPressed = 0;

    public RadioButton() {}

    public RadioButton(int value) {
        buttonPressed = value;
    }

    public int getValue() {
        return buttonPressed;
    }

    public void setValue(int button_pressed) {
        this.buttonPressed = button_pressed;
    }

}
