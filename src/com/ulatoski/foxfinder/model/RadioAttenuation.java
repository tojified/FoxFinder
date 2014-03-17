package com.ulatoski.foxfinder.model;

/**
 * Created by tojified on 3/9/14.
 */
public class RadioAttenuation {

    int[] steps = new int[] {20,20,20,10,5};  //should be ordered descending (reverse of binary value)
    int value = 0;

    public void set(int attenuationValue) {
        value = attenuationValue;
    }

    public int get() {
        return value;
    }

    public int getBinary() {  //produce a additive value based on values in steps array
        int binary = 0;
        int v = value;
        for(int i=0;i < steps.length;i++) {
            if (v >= steps[i]) {
                v = v - steps[i];                                      //subtract amount in array
                binary = setBit(binary, steps.length - i);  //turn the correct bit high
            }
        }
        return binary;
    }

    private int setBit(int number, int bit) {  //zero based from the right
        return (number | (int) Math.pow(2,bit-1));
    }

}
