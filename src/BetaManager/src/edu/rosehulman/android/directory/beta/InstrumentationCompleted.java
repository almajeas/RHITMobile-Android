package edu.rosehulman.android.directory.beta;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver used to notify BetaManager that a run of unit tests
 * has completed.
 */
public class InstrumentationCompleted extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		//reset mocking preference
		BetaPrefs.setUseMocks(context, BetaPrefs.getAlwaysUseMocks(context));
		
	}

}
