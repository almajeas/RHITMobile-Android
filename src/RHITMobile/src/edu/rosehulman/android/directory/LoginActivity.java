package edu.rosehulman.android.directory;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

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
    	RegisterTask task = new RegisterTask(username, password);
    	taskManager.addTask(task);
    	task.execute();
    }

    private class RegisterTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog dialog;
		
    	private String username;
    	private String password;
    	
    	public RegisterTask(String username, String password) {
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

			//TODO authenticate against the server
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) { }

			if ("jimmy".equals(username) || "".equals(password))
				return null;
			
			return "$3cr3tT0|<3|\\|";
		}

		@Override
    	protected void onCancelled() {
			dialog.dismiss();
			
			setResult(RESULT_CANCELED);
			finish();
		}
		
		@Override
    	protected void onPostExecute(String result) {
    		dialog.dismiss();
    		
    		if (result == null) {
    			Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
    			return;
    		}

    		User.setCookie(username, result);
	        
	        //remove ourselves from the app stack
	        setResult(Activity.RESULT_OK);
			finish();
    	}
    }

}
