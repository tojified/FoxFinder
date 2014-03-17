package com.ulatoski.foxfinder.activity;

import java.util.*;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import com.ulatoski.foxfinder.R;
import com.ulatoski.foxfinder.model.RadioSample;
import com.ulatoski.foxfinder.radio.EmulatedRadio;
import com.ulatoski.foxfinder.radio.RadioHandlerThread;

public class FoxFinderActivity extends Activity {

	private RadioHandlerThread mRadioHandlerThread;
    private boolean mDisconnect;

    private ViewGroup mPatternGraph;
    private AntennaPatternView mCurrentPattern;

    private List<RadioSample> mSamples = new ArrayList<RadioSample>();
    private List<RadioSample> mPrevRotation = mSamples;

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

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.fox_finder_activity);
        mPatternGraph = (ViewGroup) findViewById(R.id.pattern_graph);
        mCurrentPattern = new AntennaPatternView(this);
        Handler mUiHandler = new Handler(mCallback);

        mRadioHandlerThread = new EmulatedRadio(mUiHandler);
		mRadioHandlerThread.start();
	}

    @Override
	protected void onDestroy() {
		mRadioHandlerThread.quit();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		onDisconnect();
		super.onBackPressed();
	}

	private void receiveSample(RadioSample sample) {
        if (!sample.isQuality()) Log.w("RadioSample", "Bad data received! Freq:" + sample.getFrequency() + ", sMtr:" + sample.getSMeter());

        if (sample.isFirstSample()) {  //new rotation
            mSamples = new ArrayList<RadioSample>();
            nextPattern();
        }

        mSamples.add(sample);
        mCurrentPattern.setAntennaPattern(getPatternData(mSamples), getInterval(mPrevRotation));
	}

    private void nextPattern() {
        mCurrentPattern.selfDestruct(20000);
        mCurrentPattern = new AntennaPatternView(mCurrentPattern.getContext());
        mPatternGraph.addView(mCurrentPattern);
    }

    private double getInterval(List<RadioSample> samples) {
        return Math.toRadians(360) / samples.size(); //evenly spaced on polar grid
    }

    private float[] getPatternData(List<RadioSample> samples) {
        float[] data = new float[samples.size()];
        for (int i=0; i < data.length; i++) {
            RadioSample sample = samples.get(i);
            if (sample.isQuality()) {
                data[i] = (float) sample.getSMeter()/12;
            } else if (i > 0) {
                data[i] = data[i-1];
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
