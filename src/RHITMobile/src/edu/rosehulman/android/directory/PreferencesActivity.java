package edu.rosehulman.android.directory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

/**
 * An activity for managing preferences related to the app
 */
public class PreferencesActivity extends SherlockPreferenceActivity {

	public static Intent createIntent(Context context) {
		return new Intent(context, PreferencesActivity.class);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceManager().setSharedPreferencesName(MyApplication.PREFS_APP);
		getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
		
		addPreferencesFromResource(R.xml.preferences);
		
		findPreference("logout").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				logout_clicked();
				return true;
			}
		});
		
		findPreference("clearData").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearData_clicked();
				return true;
			}
		});
	}

	private void logout_clicked() {
		User.clearLogin();
		Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
	}
	
	private void clearData_clicked() {
		MyApplication.getInstance().purgeDb();
		Toast.makeText(this, "DB cleared, exiting...", Toast.LENGTH_SHORT).show();
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				Process.killProcess(Process.myPid());
			}
		}.start();
	}

}
