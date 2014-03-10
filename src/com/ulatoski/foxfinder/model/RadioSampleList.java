package com.ulatoski.foxfinder.model;

import android.util.Log;

import java.util.*;

/**
 * Created by tojified on 3/9/14.
 */
public class RadioSampleList extends ArrayList<RadioSample> {

    public RadioSampleList(int capacity) {
        super(capacity);
    }

    public RadioSampleList() {
    }

    public RadioSampleList(Collection<? extends RadioSample> collection) {
        super(collection);
    }

    public RadioSampleList overlay(RadioSampleList freshData) {
        int size = freshData.size() > this.size() ? freshData.size() : this.size();
        RadioSampleList list = new RadioSampleList();
        for (int i=0; i < size; i++) {
            list.add(freshData.size() > i ? freshData.get(i) : this.get(i));
        }
        return list;
    }

}
