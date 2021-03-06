package com.ulatoski.foxfinder.radio;

import java.io.IOException;

import android.os.Handler;
import android.os.Message;

import com.ulatoski.foxfinder.model.RadioSample;

/**
 * Reads {@link RadioSample} objects from the input stream.
 */
public class Receiver implements Runnable {

	private final RadioInputStream radioInputStream;
	private final Handler mUiHandler;

	/**
	 * Constructs the radio sample receiver.
	 * 
	 * @param inputStream
	 *            input stream from which the receiver is reading chat messages
	 * @param uiHandler
	 *            UI thread handler
	 */
	Receiver(RadioInputStream inputStream, Handler uiHandler) {
        radioInputStream = inputStream;
		mUiHandler = uiHandler;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			final RadioSample sample;

			try {
				sample = radioInputStream.readRadioSample();
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