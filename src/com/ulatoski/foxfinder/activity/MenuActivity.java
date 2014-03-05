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

import java.net.InetAddress;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ulatoski.foxfinder.R;
import com.ulatoski.foxfinder.async.FindTask;
import com.ulatoski.foxfinder.async.GenerateTask;
import com.ulatoski.foxfinder.fragment.ConnectDialogFragment;
import com.ulatoski.foxfinder.fragment.ConnectDialogFragment.OnConnectToServerListener;
import com.ulatoski.foxfinder.model.RadioSample;

/**
 * Application menu.
 */
public class MenuActivity extends Activity implements OnConnectToServerListener {

	public static final String TAG = "MULTI_THREADING";

	public static final String SERVER_MODE = "SERVER_MODE";
	public static final String SERVER_IP = "SERVER_IP";
	public static final String CLIENT_INETADDRESS = "CLIENT_INETADDRESS";
	private static final String PREF_LAST_IP = "PREF_LAST_IP";

	private static final int NUMBER_COUNT = 500000;

	private TextView mIpAddress;
	private TextView mWifiName;
	private View mClientButton;
	private View mServerButton;
	private View mGenerateButton;
	private View mFindButton;
	private ProgressBar mGenerateProgress;
	private ProgressBar mFindProgress;

	private String mLastProvidedIP;
	private String mCurrentIP;
	private SharedPreferences mSharedPreferences;
	private WifiManager mWifiManager;

	private GenerateTask mGenerateTask;
	private FindTask mFindTask;
	private double[] mNumbers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manu_activity);
		mIpAddress = (TextView) findViewById(R.id.ip_address);
		mWifiName = (TextView) findViewById(R.id.wifi_name);
		mClientButton = findViewById(R.id.menu_activity_client_button);
		mServerButton = findViewById(R.id.menu_activity_server_button);
		mFindButton = findViewById(R.id.menu_activity_greatest_value);
		mGenerateButton = findViewById(R.id.menu_activity_generate_numbers);
		mGenerateProgress = (ProgressBar) findViewById(R.id.menu_activity_generate_numbers_progress);
		mFindProgress = (ProgressBar) findViewById(R.id.menu_activity_greatest_value_progress);

		mFindButton.setEnabled(false);
		mGenerateProgress.setVisibility(View.INVISIBLE);
		mFindProgress.setVisibility(View.INVISIBLE);
		mFindProgress.setMax(NUMBER_COUNT);

		mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
		mLastProvidedIP = mSharedPreferences.getString(PREF_LAST_IP, null);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	}

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
			setWiFiInfo(wifiInfo);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		if (mWifiManager != null) {
			registerReceiver(mBroadcastReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

			if (!mWifiManager.isWifiEnabled()) {
				final WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
				setWiFiInfo(mWifiInfo);
			} else {
				setWiFiInfo(null);
			}
		} else {
			setWiFiButtonEnabled(false);
			mIpAddress.setText(getString(R.string.wifi_not_supported));
			mWifiName.setText(getString(R.string.wifi_not_supported));
		}
	}

	@Override
	protected void onPause() {
		if (mGenerateTask != null && mGenerateTask.getStatus() != AsyncTask.Status.FINISHED) {
			mGenerateTask.cancel(true);
		}
		if (mFindTask != null && mFindTask.getStatus() != AsyncTask.Status.FINISHED) {
			mFindTask.cancel(true);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	};

	private void setWiFiInfo(WifiInfo wifiInfo) {
		if (wifiInfo != null) {
			mCurrentIP = convertIPAddress(wifiInfo.getIpAddress());
			mIpAddress.setText(mCurrentIP);
			mWifiName.setText(wifiInfo.getSSID());
			setWiFiButtonEnabled(true);
		} else {
			mIpAddress.setText(getString(R.string.not_available));
			mWifiName.setText(R.string.not_available);
			setWiFiButtonEnabled(false);
		}
	}

	private void setWiFiButtonEnabled(boolean enabled) {
		mClientButton.setEnabled(enabled);
		mServerButton.setEnabled(enabled);
	}

	/**
	 * Converts given IP address from {@link int} to {@link String} representation.
	 * 
	 * @param ipAddress
	 *            IP address as integer value
	 * @return IP address as String
	 */
	public static String convertIPAddress(int ipAddress) {
		// @formatter:off
		final StringBuilder builder = new StringBuilder()
			.append(ipAddress & 0xFF)
			.append('.')
			.append(ipAddress >> 8 & 0xFF)
			.append('.')
			.append(ipAddress >> 16 & 0xFF)
			.append('.')
			.append(ipAddress >> 24 & 0xFF);
		// @formatter:on
		return builder.toString();
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.menu_activity_client_button:
			showConnectDialog();
			break;
		case R.id.menu_activity_server_button:
			startChatAsServer();
			break;
		case R.id.menu_activity_generate_numbers:
			generateNumbers();
			break;
		case R.id.menu_activity_greatest_value:
			findGreatestValue();
			break;
		default:
			throw new IllegalArgumentException(Integer.toString(view.getId()));
		}
	}

	/**
	 * Shows {@link ConnectDialogFragment}.
	 */
	public void showConnectDialog() {
		final ConnectDialogFragment fragment = new ConnectDialogFragment();
		final FragmentManager fragmentManager = getFragmentManager();
		fragment.show(fragmentManager, mLastProvidedIP);
	}

	/**
	 * Starts {@link RadioSample} as server.
	 */
	public void startChatAsServer() {
		final Intent intent = new Intent(this, RadioSample.class);
		intent.putExtra(SERVER_MODE, true);
		intent.putExtra(SERVER_IP, mCurrentIP);
		startActivity(intent);
	}

	@Override
	public void onConnectToServer(InetAddress address) {
		final Editor editor = mSharedPreferences.edit();
		mLastProvidedIP = address.getHostAddress();
		editor.putString(PREF_LAST_IP, mLastProvidedIP);
		editor.commit();

		final Intent intent = new Intent(this, RadioSample.class);
		intent.putExtra(SERVER_MODE, false);
		intent.putExtra(CLIENT_INETADDRESS, address);
		startActivity(intent);
	}

	private void generateNumbers() {
		mNumbers = new double[NUMBER_COUNT];
		mGenerateTask = new GenerateTask(mFindButton, mGenerateButton, mGenerateProgress);
		mGenerateTask.execute(mNumbers);
	}

	private void findGreatestValue() {
		mFindTask = new FindTask(this, mFindButton, mGenerateButton, mFindProgress);
		mFindTask.execute(mNumbers);
	}

}
