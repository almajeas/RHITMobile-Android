package edu.rosehulman.android.directory;
import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.model.AuthenticationResponse;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

/**
 * Activity used to register users for the beta program
 */
public class LoginActivity extends SherlockActivity {

	public static Intent createIntent(Context context) {
		return new Intent(context, LoginActivity.class);
	}
	
	private TaskManager taskManager = new TaskManager();

    private TextView txtUsername;
    private TextView txtPassword;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        getSupportActionBar().setHomeButtonEnabled(true);
        
        txtUsername = (TextView)findViewById(R.id.username);
        txtPassword = (TextView)findViewById(R.id.password);
        
        findViewById(R.id.back).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				btnBack_onClick();
			}
        });
        
        findViewById(R.id.login).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				btnLogin_onClick();
			}
        });
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	taskManager.abortTasks();
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			default:
				return super.onOptionsItemSelected(item); 
		}
		return true;
	}
    
    private void btnBack_onClick() {
    	setResult(RESULT_CANCELED);
    	finish();
    }
    
    private void btnLogin_onClick() {
    	
    	//get our registration data
    	String username = txtUsername.getText().toString().toLowerCase();
    	String password = txtPassword.getText().toString();
    	
    	//make sure required fields are populated
    	if ("".equals(username)) {
    		Toast.makeText(this, "A username is required", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	if ("".equals(password)) {
    		Toast.makeText(this, "A password is required", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	//start the registration process
    	LoginTask task = new LoginTask(username, password);
    	taskManager.addTask(task);
    	task.execute();
    }

    private class LoginTask extends AsyncTask<Void, Integer, String> {

		private ProgressDialog dialog;
		
    	private String username;
    	private String password;
    	
    	private boolean serverError;
    	
    	public LoginTask(String username, String password) {
    		this.username = username;
    		this.password = password;
    	}

    	@Override
    	protected void onPreExecute() {
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setTitle(null);
			dialog.setMessage("Logging in...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			dialog.show();
    	}
    	
		@Override
		protected String doInBackground(Void... args) {

			MobileDirectoryService service = new MobileDirectoryService();
			AuthenticationResponse response = null;
			serverError = false;
			
			for (int attempt = 2; ; attempt++) {
				try {
					response = service.login(username, password);
					if (response == null)
						return null;
					return response.token;
					
				} catch (ClientException e) {
					//invalid username or password
					return null;
					
				} catch (ServerException e) {
					Log.e(C.TAG, "Server is not accepting authentication requests", e);
					serverError = true;
					return null;
					
				} catch (JSONException e) {
					Log.e(C.TAG, "An error occured while parsing the JSON response", e);
					serverError = true;
					return null;
					
				} catch (IOException e) {
					Log.e(C.TAG, "Failed to authenticate user, retrying...", e);
				}

				if (isCancelled())
					return null;
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {}
				publishProgress(attempt);
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... status) {
			int attempt = status[0];
			dialog.setMessage(String.format("Logging in (attempt %d)...", attempt));
		}

		@Override
    	protected void onCancelled(String result) {
			dialog.dismiss();
		}
		
		@Override
    	protected void onPostExecute(String result) {
    		dialog.dismiss();
    		
    		if (result == null) {
    			if (serverError) {
    				Toast.makeText(LoginActivity.this, "Authentication service is rejecting requests.  Please try again later.", Toast.LENGTH_SHORT).show();
    			} else {
    				Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
    			}
    			return;
    		}

    		User.setCookie(username, result);
	        
	        //remove ourselves from the app stack
	        setResult(Activity.RESULT_OK);
			finish();
    	}
    }

}
