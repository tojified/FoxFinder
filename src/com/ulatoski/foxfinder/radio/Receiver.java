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
package com.ulatoski.foxfinder.radio;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;

import com.ulatoski.foxfinder.model.RadioSample;

/**
 * Reads {@link RadioSample} objects from the input stream.
 */
public class Receiver implements Runnable {

	private final RadioSampleInputStream radioSampleInputStream;
	private final Handler mUiHandler;

	/**
	 * Constructs the radio sample receiver.
	 * 
	 * @param inputStream
	 *            input stream from which the receiver is reading chat messages
	 * @param uiHandler
	 *            UI thread handler
	 */
	Receiver(RadioSampleInputStream inputStream, Handler uiHandler) {
        radioSampleInputStream = inputStream;
		mUiHandler = uiHandler;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			final RadioSample sample;

			try {
				sample = radioSampleInputStream.readRadioSample();
			    if (sample == null) { onDisconnect("Disconnected!"); return; }
            } catch (IOException e) {
				onError(e.getMessage());
				return;
			}

			mUiHandler.sendMessage(Message.obtain(null, RadioHandlerThread.ON_RECEIVE_SAMPLE, sample));
		}
	}

	/**
	 * Sends error message to the activity.
	 * 
	 * @param message
	 *            an error message
	 */
	void onError(String message) {
		mUiHandler.sendMessage(Message.obtain(null, RadioHandlerThread.ON_ERROR, message));
	}

    /**
     * Sends error message to the activity.
     *
     * @param message
     *            an error message
     */
    void onDisconnect(String message) {
        mUiHandler.sendMessage(Message.obtain(null, RadioHandlerThread.ON_DISCONNECT, message));
    }

}