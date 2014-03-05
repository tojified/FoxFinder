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

package com.ulatoski.foxfinder.fragment;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ulatoski.foxfinder.R;

/**
 * Dialog with a field to input an IP address.
 */
public class ConnectDialogFragment extends DialogFragment implements OnEditorActionListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.connect_dialog_fragment, container);
		final TextView address = (TextView) view.findViewById(R.id.connect_dialog_ip_address);
		address.setOnEditorActionListener(this);
		address.setText(getTag() == null ? "" : getTag());
		address.requestFocus();

		final Dialog dialog = getDialog();
		dialog.setTitle(getString(R.string.server_ip_address));
		dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		return view;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		final String address = v.getText().toString();

		if (!address.isEmpty()) {
			// Obtains InetAddress at a background thread.
			new Thread(new Runnable() {

				@Override
				public void run() {
					final InetAddress inetAddress;
					try {
						inetAddress = InetAddress.getByName(address);
					} catch (UnknownHostException e) {
						showToast(e.getMessage());
						return;
					}

					if (inetAddress == null) {
						showToast(getString(R.string.wrong_ip_address));
					} else {
						connect(inetAddress);
					}
				}
			}).start();
		}

		return true;
	}

	/**
	 * Shows toast message on the UI thread. This method is safe to call from background thread.
	 * 
	 * @param message
	 *            message to show
	 */
	private void showToast(final String message) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Calls {@link OnConnectToServerListener#onConnectToServer(InetAddress)} method and dismisses this dialog fragment.
	 * This method is safe to call it from background thread.
	 * 
	 * @param inetAddress
	 *            {@link InetAddress} of the server
	 */
	private void connect(final InetAddress inetAddress) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final OnConnectToServerListener listener = (OnConnectToServerListener) getActivity();
				listener.onConnectToServer(inetAddress);
				dismiss();
			}
		});
	}

	/**
	 * Provides a method called when the application starts to connect to the server.
	 */
	public interface OnConnectToServerListener {

		/**
		 * Called when the entered IP address has been accepted by the user.
		 * 
		 * @param inetAddress
		 *            {@link InetAddress} of the server
		 */
		void onConnectToServer(InetAddress inetAddress);

	}
}
