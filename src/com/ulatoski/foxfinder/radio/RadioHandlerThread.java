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

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Radio handler thread.
 */
public class RadioHandlerThread extends HandlerThread {

	/**
	 * Streaming error.
	 */
	public static final int ON_ERROR = 0;
	/**
	 * Successful server-client connection.
	 */
	public static final int ON_CONNECT = 1;
	/**
	 * Terminated connection between a client and a server.
	 */
	public static final int ON_DISCONNECT = 2;
	/**
	 * New sample has been received.
	 */
	public static final int ON_RECEIVE_SAMPLE = 4;

	private final ExecutorService executorService;
	private final Handler uiHandler;

    EmulatedBluetoothSocket socket;

	private RadioInputStream inputStream;

	public RadioHandlerThread(String name, Handler uiHandler) {
		super(name);
		this.uiHandler = uiHandler;
		executorService = Executors.newSingleThreadExecutor();
	}

	@Override
	protected void onLooperPrepared() {
		onInitializeStreams();
		startReceiver();
		super.onLooperPrepared();
	}

	@Override
	public boolean quit() {
		close(socket);
		close(inputStream);
		executorService.shutdownNow();
		return super.quit();
	}

	/**
	 * Sends error message to the activity via UI handler.
	 * 
	 * @param message
	 *            an error message
	 */
	void onError(String message) {
		uiHandler.sendMessage(Message.obtain(null, ON_ERROR, message));
	}

	/**
	 * Sends connect message to the activity via UI handler.
	 */
	void onConnect() {
        uiHandler.sendEmptyMessage(ON_CONNECT);
	}

	private void startReceiver() {
        Receiver sampleReceiver = new Receiver(inputStream, uiHandler);
		executorService.execute(sampleReceiver);
	}

	private void onInitializeStreams() {
		try {
			inputStream = new RadioInputStream(socket.getInputStream());
		} catch (Exception e) {
			close(inputStream);
			onError(e.getMessage());
		}
	}

    void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// Does nothing intentionally.
			}
		}
	}

	void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// Does nothing intentionally.
			}
		}
	}
}
