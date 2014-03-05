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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.ulatoski.foxfinder.R;
import com.ulatoski.foxfinder.model.RadioSample;
import com.ulatoski.foxfinder.radio.EmulatedRadio;
import com.ulatoski.foxfinder.radio.RadioHandlerThread;

public class FoxFinderActivity extends Activity {

    private MessageAdapter messageAdapter;
	private RadioHandlerThread handlerThread;
    private boolean mDisconnect;

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

			Log.e(MenuActivity.TAG, "" + wifiState);
			if (wifiState == WifiManager.WIFI_STATE_DISABLING || wifiState == WifiManager.WIFI_STATE_DISABLED) {
				onDisconnect();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        ListView mListView = (ListView) findViewById(R.id.chat_activity_messages);

        Handler mUiHandler = new Handler(mCallback);

        messageAdapter = new MessageAdapter(this);
        mListView.setAdapter(messageAdapter);

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

	/**
	 * Called when chat message has been received. Adds the message to the {@link MessageAdapter} adapter.
	 * 
	 * @param sample
	 *            {@link RadioSample}
	 */
	private void receiveSample(RadioSample sample) {
		messageAdapter.addMessage(sample);
	}

	/**
	 * Adapter for the list view. Holds the chat messages.
	 */
	private static class MessageAdapter extends BaseAdapter {

		private static final int MAX_CHAT_MESSAGES_COUNT = 40;

		private final List<RadioSample> mMessages;
		private final Context mContext;

		public MessageAdapter(Context context) {
			super();
			mContext = context;
			mMessages = new ArrayList<RadioSample>(MAX_CHAT_MESSAGES_COUNT);
		}

		/**
		 * Adds chat message at the end. If the {@link MessageAdapter#MAX_CHAT_MESSAGES_COUNT} limit has been reached
		 * removes first element.
		 * 
		 * @param message
		 *            {@link com.ulatoski.foxfinder.model.RadioSample}
		 */
		public void addMessage(RadioSample message) {
			if (mMessages.size() >= MAX_CHAT_MESSAGES_COUNT) {
				mMessages.remove(0);
				notifyDataSetChanged();
			}
			mMessages.add(message);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mMessages.size();
		}

		@Override
		public RadioSample getItem(int position) {
			return mMessages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RadioSample sample = getItem(position);

			int layoutId;
			if (!sample.isFirstSample()) {
				layoutId = R.layout.message_view;
			} else {
				layoutId = R.layout.stranger_message_view;
			}

			convertView = View.inflate(mContext, layoutId, null);
			TextView text = (TextView) convertView.findViewById(R.id.message_view_message);
			text.setText(sample.getFrequency() + " : S"+sample.getSMeter() + " : " + sample.isFirstSample());
			return convertView;
		}
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
