package edu.rosehulman.android.directory.beta;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Main activity launched from the MobileDirecory homescreen
 * Even if the beta manager is disabled, this screen can be accessed via
 * the menu on the homescreen.
 */
public class BetaManagerActivity extends Activity {
	
	private static String TAG = "BetaManager";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        View btnUnitTests;
        btnUnitTests = findViewById(R.id.btnUnitTests);
        btnUnitTests.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				btnUnitTests_onClick();
			}
		});
        
        findViewById(R.id.btnUnregister).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//mark that we are not registered
			    //TODO tell the server also?
			    BetaPrefs.setRegistered(BetaManagerActivity.this, false);
			}
		});
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle item selection
        switch (item.getItemId()) {
        case R.id.preferences:
            startActivity(new Intent(this, BetaPreferencesActivity.class));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void btnUnitTests_onClick() {
    	boolean started;
    	Log.w(TAG, "Starting instrumentation");
    	
    	BetaPrefs.setUseMocks(this, true);

    	ComponentName name;
    	name = new ComponentName(getApplicationContext(), CustomInstrumentationTestRunner.class);
    	started = getApplicationContext().startInstrumentation(name, null, null);
    	if (started) {
    		Log.w(TAG, "Instrumentation started");
    	}
    }
}