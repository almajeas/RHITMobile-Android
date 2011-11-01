package edu.rosehulman.android.directory.beta;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import edu.rosehulman.android.directory.beta.service.BetaService;

/**
 * Activity used to register users for the beta program
 */
public class RegisterActivity extends Activity {
    
    private TextView txtEmail;
    private TextView txtName;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        
        txtEmail = (TextView)findViewById(R.id.txtEmail);
        txtName = (TextView)findViewById(R.id.txtName);
        
        findViewById(R.id.btnBack).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				btnBack_onClick();
			}
        });
        
        findViewById(R.id.btnRegister).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				btnRegister_onClick();
			}
        });
    }
    
    private void btnBack_onClick() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder
    		.setMessage(R.string.abort_register_message)
    		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//go away, for now
					setResult(Activity.RESULT_CANCELED);
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
    
    private void btnRegister_onClick() {
    	
    	//get our registration data
    	String email = txtEmail.getText().toString();
    	String name = txtName.getText().toString();
    	
    	//make sure required fields are populated
    	if ("".equals(email) || 
    			!Pattern.compile(".*@.*\\..*").matcher(email).matches()) {
    		Toast.makeText(this, "A valid email address is required", 1000).show();
    		return;
    	}
    	
    	//start the registration process
    	new RegisterTask(this, email, name).execute();    	
    }

    private class RegisterTask extends AsyncTask<Void, Void, Boolean> {
    	
    	private Context m_context;
    	private ProgressDialog m_status;
    	
    	private String m_email;
    	private String m_name;
    	
    	public RegisterTask(Context context, String email, String name) {
    		m_context = context;
    		m_email = email;
    		m_name = name;
    	}

    	@Override
    	protected void onPreExecute() {
    		String title = getString(R.string.title_registering);
    		String message = getString(R.string.details_registering);
        	m_status = ProgressDialog.show(m_context, title, message, false);
    	}
    	
		@Override
		protected Boolean doInBackground(Void... arg0) {
			
			long ticks = System.currentTimeMillis();
			
			Boolean res = register(m_email, m_name);
			
			if (res) {
				try {
					//take at least 2 seconds to execute
					Thread.sleep(2000 - Math.min(2000, (System.currentTimeMillis() - ticks)));
				} catch (InterruptedException e) { }
			}
			
			return res;
		}
		
		@Override
    	protected void onPostExecute(Boolean result) {
    		m_status.dismiss();
    		
    		if (result) {
    			//mark that we are registered
    			BetaPrefs.setRegistered(RegisterActivity.this, true);
    	        
    	        //jump to the startup activity
    	        startActivity(new Intent(RegisterActivity.this, StartupActivity.class));
    	        
    	        //remove ourselves from the app stack
    	        setResult(Activity.RESULT_OK);
    			finish();
    		} else {
    			Toast.makeText(m_context, "An error occurred while registering", 2000).show();
    		}
    	}
		

	    private String getDeviceIdentifier() {
	    	return Secure.getString(getContentResolver(), Secure.ANDROID_ID);
	    }
	    
	    private String getCarrier() {
	    	TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    	return manager.getNetworkOperatorName();
	    }
	    
	    private String getOSInfo() {
	    	String procVersion;
	    	try {
	    		 BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
	    		 try {
	    			 procVersion = reader.readLine();
	    		 } finally {
	    			 reader.close();
	    		 }
	    	} catch (IOException ex) {
	    		procVersion = "Unavailable";
	    	}
	    	
	    	try {
				return new JSONObject().
						put("display", Build.DISPLAY).
						put("sdk", Build.VERSION.SDK).
						put("release", Build.VERSION.RELEASE).
						put("version", procVersion).
						toString();
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
	    }
	    
	    private String getModel() {
	    	try {
				return new JSONObject().
						put("manufacturer", Build.MANUFACTURER).
						put("device", Build.DEVICE).
						put("model", Build.MODEL).
						toString();
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
	    }
	    
	    private String getBuild() {
	    	PackageInfo packageInfo;
	    	try {
	    		packageInfo = getPackageManager().getPackageInfo("edu.rosehulman.android.directory", 0); 
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				return null;
			}
			return String.valueOf(packageInfo.versionCode);
	    }

        private boolean register(String email, String name) {

        	try {
        		BetaService service = new BetaService();
        		String authToken = service.register(email, getDeviceIdentifier(), getBuild(),
        				getOSInfo(), getModel(),
        				name, getCarrier(), null);
    	    	
    			if (authToken == null) {
    				return false;
    			}
    			
    			//save our auth token
    			Resources res = getResources();
    			
    			SharedPreferences prefs = getSharedPreferences(res.getString(R.string.prefs_main), MODE_PRIVATE);
    			Editor edit = prefs.edit();
    	        edit.putString(res.getString(R.string.pref_auth_token), authToken);
    	        edit.commit();
    	        
    		} catch (Exception e) {
    			e.printStackTrace();
    			return false;
    		}
        	
        	return true;
        }
    }
}
