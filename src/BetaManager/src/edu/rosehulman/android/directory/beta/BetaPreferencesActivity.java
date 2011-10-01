package edu.rosehulman.android.directory.beta;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Activity that allows the user to modify how MobileDirectory
 * functions.  Any beta related feature should be configurable here.
 */
public class BetaPreferencesActivity extends PreferenceActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName("PREFS_BETA");
		getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
		
		addPreferencesFromResource(R.xml.beta_preferences);
	}

}
