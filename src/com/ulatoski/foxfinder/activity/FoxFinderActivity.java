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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.ulatoski.foxfinder.R;
import com.ulatoski.foxfinder.model.RadioSample;
import com.ulatoski.foxfinder.radio.EmulatedRadio;
import com.ulatoski.foxfinder.radio.RadioHandlerThread;

public class FoxFinderActivity extends Activity {

	private RadioHandlerThread handlerThread;
    private boolean mDisconnect;

    private List<RadioSample> mSamples = new ArrayList<RadioSample>();
    private List<List> patternHistory = new ArrayList<List>();

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

    @SuppressWarnings("unchecked")
	private void receiveSample(RadioSample sample) {

        if (patternHistory.size() > 0) {
            AntennaPatternView view = (AntennaPatternView) (findViewById(R.id.antenna_pattern));
            float[] patternData = getPatternData(mSamples, patternHistory.get(patternHistory.size()-1));
            view.setAntennaPattern(patternData); //draw previous rotation
        }
        if (sample.isFirstSample()) {
            if (!mSamples.isEmpty()) patternHistory.add(mSamples);
            mSamples = new ArrayList<RadioSample>(); //new rotation
        }
        mSamples.add(sample);
	}

    private float[] getPatternData(List<RadioSample> samples, List<RadioSample> lastRotation) {
        int count = samples.size();
        float[] data = count > lastRotation.size() ? new float[count] : new float[lastRotation.size()];
        for (int i = 0; i < data.length; i++) {
            RadioSample sample = i < count ? samples.get(i) : lastRotation.get(i);  //get the new if available
            if (sample.isQuality()) {
                data[i] = (float) sample.getSMeter()/12;
            } else {
                data[i] = (float) lastRotation.get(i).getSMeter()/12;
                Log.w("RadioSample", "Bad data received! Freq:" + sample.getFrequency() + ", sMtr:" + sample.getSMeter());
            }
        }
        return data;
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
