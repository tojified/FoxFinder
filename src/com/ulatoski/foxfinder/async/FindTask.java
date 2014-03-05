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

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Finds the greatest element in the provided array.
 */
public class FindTask extends AsyncTask<double[], Integer, Double> {

	private final Context mContext;
	private final View mFindButton;
	private final View mGenerateButton;
	private final ProgressBar mFindProgress;

	/**
	 * Creates a task to find the greatest element in the provided array.
	 * 
	 * @param context
	 *            application context
	 * @param findButton
	 *            view which triggers this task to start
	 * @param generateButton
	 *            view which will be enabled after task is executed
	 * @param findProgress
	 *            progress bar to show the operation progress
	 */
	public FindTask(Context context, View findButton, View generateButton, ProgressBar findProgress) {
		super();
		mContext = context;
		mFindButton = findButton;
		mGenerateButton = generateButton;
		mFindProgress = findProgress;
	}

	@Override
	protected void onPreExecute() {
		mFindButton.setEnabled(false);
		mFindProgress.setVisibility(View.VISIBLE);
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Double result) {
		mGenerateButton.setEnabled(true);
		mFindProgress.setVisibility(View.INVISIBLE);
		Toast.makeText(mContext, Double.toString(result), Toast.LENGTH_LONG).show();
		super.onPostExecute(result);
	}

	@Override
	protected Double doInBackground(double[]... params) {
		double[] numbers = params[0];
		double result = Double.MIN_VALUE;

		for (int i = 0; i < numbers.length && !isCancelled(); ++i) {
			if (i % 20 == 0) {
				publishProgress(i);
			}

			if (numbers[i] > result) {
				result = numbers[i];
			}
		}

		return result;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mFindProgress.setProgress(values[0]);
		super.onProgressUpdate(values);
	}

	@Override
	protected void onCancelled(Double result) {
		mFindProgress.setVisibility(View.INVISIBLE);
		mFindButton.setEnabled(true);
		super.onCancelled(result);
	}

}