package com.ulatoski.foxfinder.radio;

import java.io.IOException;

import android.os.Environment;
import android.os.Handler;
import com.ulatoski.foxfinder.radio.emulated.BluetoothSocket;

/**
 * Client side handler thread.
 */
public class EmulatedRadio extends RadioHandlerThread {

	/**
	 * Constructs the client side handler thread.
	 * 
	 * @param uiHandler
	 *            UI thread handler
	 */
	public EmulatedRadio(Handler uiHandler) {
		super(EmulatedRadio.class.getSimpleName(), uiHandler);
	}

	@Override
	protected void onLooperPrepared() {
		try {
            socket = new BluetoothSocket(Environment.getExternalStorageDirectory(), "RadioData.txt");
		} catch (IOException e) {
			close(socket);
            e.printStackTrace();
			onError(e.getMessage());
			return;
		}
		super.onLooperPrepared();
	}

}
