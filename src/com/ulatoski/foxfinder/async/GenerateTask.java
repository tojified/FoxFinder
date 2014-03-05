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
package com.ulatoski.foxfinder.async;

import java.util.Arrays;
import java.util.Random;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Fills the provided array with generated values.
 */
public class GenerateTask extends AsyncTask<double[], Void, Void> {

	private final View mFindButton;
	private final View mGenerateButton;
	private final ProgressBar mGenerateProgress;
	private double[] mNumbers;

	/**
	 * Creates a task to fill array with generated double values.
	 * 
	 * @param findButton
	 *            view which will be enabled after task is executed
	 * @param generateButton
	 *            view which triggers this task to start
	 * @param generateProgress
	 *            progress bar to show the operation progress
	 */
	public GenerateTask(View findButton, View generateButton,
			ProgressBar generateProgress) {
		super();
		mFindButton = findButton;
		mGenerateButton = generateButton;
		mGenerateProgress = generateProgress;
	}

	@Override
	protected void onPreExecute() {
		mGenerateButton.setEnabled(false);
		mGenerateProgress.setVisibility(View.VISIBLE);
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Void result) {
		mGenerateProgress.setVisibility(View.INVISIBLE);
		mFindButton.setEnabled(true);
		super.onPostExecute(result);
	}

	@Override
	protected Void doInBackground(double[]... params) {
		mNumbers = params[0];
		Random random = new Random();

		for (int i = 0; i < mNumbers.length && !isCancelled(); ++i) {
			mNumbers[i] = random.nextDouble() * 100.0f;
		}

		if (!isCancelled()) {
			SystemClock.sleep(2000);
		}
		return null;
	}

	@Override
	protected void onCancelled(Void result) {
		mGenerateProgress.setVisibility(View.INVISIBLE);
		mGenerateButton.setEnabled(true);
		Arrays.fill(mNumbers, 0.0f);
		super.onCancelled(result);
	}

}