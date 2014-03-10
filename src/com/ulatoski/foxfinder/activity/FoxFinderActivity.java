/*
 ********************************************************************************
 * Copyright (c) 2012 Samsung Electronics, Inc.
 * All rights reserved.
 *
 * This software is a confidential and proprietary information of Samsung
 * Electronics, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Samsung Electronics.
 ********************************************************************************
 */
package com.ulatoski.foxfinder.activity;

import java.util.*;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import com.ulatoski.foxfinder.R;
import com.ulatoski.foxfinder.model.RadioSample;
import com.ulatoski.foxfinder.radio.EmulatedRadio;
import com.ulatoski.foxfinder.radio.RadioHandlerThread;

public class FoxFinderActivity extends Activity {

	private RadioHandlerThread handlerThread;
    private boolean mDisconnect;

    private ViewGroup mPatternGraph;
    private AntennaPatternView mCurrentPattern;

    private List<RadioSample> mSamples = new ArrayList<RadioSample>();
    private List<AntennaPatternView> viewHistory = new ArrayList<AntennaPatternView>();
    private List<List<RadioSample>> radioSampleHistory = new ArrayList<List<RadioSample>>();


	private final Handler.Callback mCallback = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case RadioHandlerThread.ON_ERROR:
				onError((String) msg.obj);
				break;
			case RadioHandlerThread.ON_DISCONNECT:
				onDisconnect();
				break;
			case RadioHandlerThread.ON_RECEIVE_SAMPLE:
				receiveSample((RadioSample) msg.obj);
				break;
			default:
				throw new IllegalArgumentException(Integer.toString(msg.what));
			}
			return true;
		}
	};

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

			if (wifiState == WifiManager.WIFI_STATE_DISABLING || wifiState == WifiManager.WIFI_STATE_DISABLED) {
				onDisconnect();
			}
		}
	};


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.fox_finder_activity);
        mPatternGraph = (ViewGroup) findViewById(R.id.pattern_graph);
        mCurrentPattern = new AntennaPatternView(this);
        Handler mUiHandler = new Handler(mCallback);

        handlerThread = new EmulatedRadio(mUiHandler);
		handlerThread.start();
		registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
	}

    @Override
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		handlerThread.quit();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		onDisconnect();
		super.onBackPressed();
	}

	private void receiveSample(RadioSample sample) {
        if (!sample.isQuality()) Log.w("RadioSample", "Bad data received! Freq:" + sample.getFrequency() + ", sMtr:" + sample.getSMeter());

        if (sample.isFirstSample()) {  //new rotation
            if (!mSamples.isEmpty()) {
                radioSampleHistory.add(mSamples);
                mCurrentPattern.setAntennaPattern(getPatternData(mSamples));    //ensure historic pattern has accurate sample count
                viewHistory.add(mCurrentPattern);                               //unless it is the first rotation, store it!
                mSamples = new ArrayList<RadioSample>();                               //and start with a fresh array
                for (AntennaPatternView view : viewHistory) {
                     view.setAlpha(view.getAlpha()/4);
                }
            }
            mCurrentPattern = new AntennaPatternView(mCurrentPattern.getContext());
            mPatternGraph.addView(mCurrentPattern);
        }

        if (radioSampleHistory.size() > 0) {                                    //if there is history, use it to complete the data
            List<RadioSample> lastPatternData = radioSampleHistory.get(radioSampleHistory.size()-1);
            mCurrentPattern.setAntennaPattern(getPatternData(merge(mSamples, lastPatternData)));
        } else if (mSamples.size() > 0) {
            mCurrentPattern.setAntennaPattern(getPatternData(mSamples));        //otherwise just start drawing
        }
        mSamples.add(sample);
	}

    private float[] getPatternData(List<RadioSample> samples) {
        float[] data = new float[samples.size()];
        for (int i=0; i < data.length; i++) {
            RadioSample sample = samples.get(i);
            if (sample.isQuality()) {
                data[i] = (float) sample.getSMeter()/12;
            } else if (i > 0) {
                data[i] = data[i-1];
                Log.w("RadioSample", "Bad data received! Freq:" + sample.getFrequency() + ", sMtr:" + sample.getSMeter());
            }
        }
        return data;
    }

    private List<RadioSample> merge(List<RadioSample> freshData, List<RadioSample> oldData) {
        int size = freshData.size() > oldData.size() ? freshData.size() : oldData.size();
        List<RadioSample> list = new ArrayList<RadioSample>();
        for (int i=0; i < size; i++) {
            list.add(freshData.size() > i ? freshData.get(i) : oldData.get(i));
        }
        return list;
    }

	/**
	 * Shows toast with the error message and finishes {@link RadioSample}.
	 * 
	 * @param message
	 *            message to show as a toast
	 */
	private void onError(String message) {
		if (!mDisconnect) {
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	/**
	 * Shows toast message and finishes {@link RadioSample}.
	 */
	private void onDisconnect() {
		if (!mDisconnect) {
			mDisconnect = true;
			Toast.makeText(this, R.string.disconnected, Toast.LENGTH_LONG).show();
			finish();
		}
	}

}
