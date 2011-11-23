package edu.rosehulman.android.directory.beta;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import edu.rosehulman.android.directory.beta.service.BetaService;

/**
 * Main activity launched from the MobileDirecory homescreen
 * Even if the beta manager is disabled, this screen can be accessed via
 * the menu on the homescreen.
 */
public class BetaManagerActivity extends Activity {
	
	private EditText txtFeedback;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.btnFeedback).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnFeedback_onClick();
			}
		});
        
        findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				btnBack_onClick();
			}
		});
        
        txtFeedback = (EditText)findViewById(R.id.txtFeedback);
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
        case R.id.unregister:
        	menu_unregister();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void menu_unregister() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder
    		.setMessage("You probably don't want to do this.  Are you sure you want to unregister?")
    		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
			 		//mark that we are not registered
					
				    BetaPrefs.setRegistered(BetaManagerActivity.this, false);
			    	finish();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//do nothing
				}
			})
			.show();
    }
    
    private void btnBack_onClick() {
    	finish();
    }
    
    private void btnFeedback_onClick() {
    	String feedback = txtFeedback.getText().toString();
    	
    	//TODO prompt to run unit tests
    	new SubmitFeedback().execute(feedback);
    }
    
    /*
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
    */
    
    private class SubmitFeedback extends AsyncTask<String, Void, Boolean> {
    	
    	private ProgressDialog status;
    	
    	@Override
    	protected void onPreExecute() {

    		String title = "Submitting Feedback";
    		String message = "";
        	status = ProgressDialog.show(BetaManagerActivity.this, title, message, true, false);
    	}

		@Override
		protected Boolean doInBackground(String... args) {
			String feedback = args[0];
			
			Resources res = getResources();
			
			SharedPreferences prefs = getSharedPreferences(res.getString(R.string.prefs_main), MODE_PRIVATE);
			String authToken = prefs.getString(res.getString(R.string.pref_auth_token), null);
			
			BetaService service = new BetaService();
			try {
				return service.postFeedback(authToken, feedback);
			} catch (Exception e) {
				Log.e(C.TAG, "Failed to post feedback", e);
				return false;
			}
		}
    	
		@Override
    	protected void onPostExecute(Boolean success) {
			status.dismiss();
			
			String message;
			if (success) {
				message = "Thanks";
			} else {
				message = "An error occurred while submitting feedback";
			}
			
			Toast.makeText(BetaManagerActivity.this, message, Toast.LENGTH_SHORT).show();
			
			finish();
    		
    	}
    	
    }
}