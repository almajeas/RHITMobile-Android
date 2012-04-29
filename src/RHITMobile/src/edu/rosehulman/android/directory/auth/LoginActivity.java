package edu.rosehulman.android.directory.auth;
import java.io.IOException;

import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
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

import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.TaskManager;
import edu.rosehulman.android.directory.model.BannerAuthResponse;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

/**
 * Activity used to register users for the beta program
 */
public class LoginActivity extends SherlockActivity {
	
	public static final String ACTION_NEW_ACCOUNT = "NewAccount";
	public static final String ACTION_UPDATE_ACCOUNT = "UpdateAccount";
	
	public static final String KEY_ACCOUNT = "Account";

	public static Intent createIntentForNewAccount(Context context, AccountAuthenticatorResponse response) {
		Intent intent = new Intent(context, LoginActivity.class);
		intent.setAction(ACTION_NEW_ACCOUNT);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		return intent;
	}
	
	public static Intent createIntentForUpdateAccount(Context context, AccountAuthenticatorResponse response, Account account) {
		Intent intent = new Intent(context, LoginActivity.class);
		intent.setAction(ACTION_UPDATE_ACCOUNT);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		intent.putExtra(KEY_ACCOUNT, account);
		return intent;
	}
	
	private TaskManager taskManager = new TaskManager();

    private TextView txtUsername;
    private TextView txtPassword;
    
    private AccountAuthenticatorResponse mResponse;
    private Account mAccount;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        getSupportActionBar().setHomeButtonEnabled(true);
        
        txtUsername = (TextView)findViewById(R.id.username);
        txtPassword = (TextView)findViewById(R.id.password);
        
        Intent intent = getIntent();
        
        if (!intent.hasExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)) {
        	setResult(RESULT_CANCELED);
        	finish();
        	return;
        }
        Bundle extras = intent.getExtras();
        mResponse = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        
        if (ACTION_NEW_ACCOUNT.equals(intent.getAction())) {
        	
        } else if (ACTION_UPDATE_ACCOUNT.equals(intent.getAction())) {
        	mAccount = extras.getParcelable(KEY_ACCOUNT);
        	txtUsername.setText(mAccount.name);
        	txtUsername.setEnabled(false);
        	((TextView)findViewById(R.id.title)).setText(R.string.update_login_message);
        	
        } else {
        	mResponse.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS, "Invalid action");
        	setResult(RESULT_CANCELED);
        	finish();
        	return; 
        }
        
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
				setResult(Activity.RESULT_CANCELED);
				mResponse.onError(AccountManager.ERROR_CODE_CANCELED, "Leaving login task");
				finish();
				break;
			default:
				return super.onOptionsItemSelected(item); 
		}
		return true;
	}
	
	@Override
	public void onBackPressed() {
		setResult(Activity.RESULT_CANCELED);
		mResponse.onError(AccountManager.ERROR_CODE_CANCELED, "Leaving login task");
		super.onBackPressed();
	}
    
    private void btnBack_onClick() {
        //remove ourselves from the app stack
        setResult(Activity.RESULT_CANCELED);
        mResponse.onError(AccountManager.ERROR_CODE_CANCELED, "Leaving login task");
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
    
    private void processAuthentication(String username, String password, BannerAuthResponse auth) {
    	
    	AccountManager manager = AccountManager.get(LoginActivity.this);
    	
    	String action = getIntent().getAction();
    	
    	if (ACTION_NEW_ACCOUNT.equals(action)) {
	    	Account account = new Account(username, AccountAuthenticator.ACCOUNT_TYPE);
			boolean accountCreated = manager.addAccountExplicitly(account, password, null);
			
			if (!accountCreated) {
				mResponse.onError(AccountManager.ERROR_CODE_BAD_REQUEST, "Failed to create account");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			
			if (accountCreated) {
				Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
				mResponse.onResult(result);
				
				manager.setAuthToken(account, AccountAuthenticator.TOKEN_TYPE, auth.token);
			}
			
    	} else if (ACTION_UPDATE_ACCOUNT.equals(action)) {
			Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, mAccount.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
			result.putString(AccountManager.KEY_AUTHTOKEN, auth.token);
			result.putLong(AccountAuthenticator.KEY_EXPIRATION_TIME, auth.expiration.getTime());
			mResponse.onResult(result);
    		
			manager.setPassword(mAccount, password);
    		manager.setAuthToken(mAccount, AccountAuthenticator.TOKEN_TYPE, auth.token);
    	}

        //remove ourselves from the app stack
        setResult(Activity.RESULT_OK);
		finish();
    }

    private class LoginTask extends AsyncTask<Void, Integer, BannerAuthResponse> {

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
		protected BannerAuthResponse doInBackground(Void... args) {

			MobileDirectoryService service = new MobileDirectoryService();
			BannerAuthResponse response = null;
			serverError = false;
			
			for (int attempt = 2; ; attempt++) {
				try {
					response = service.login(username, password);
					if (response == null)
						return null;
					return response;
					
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
    	protected void onCancelled(BannerAuthResponse result) {
			dialog.dismiss();
		}
		
		@Override
    	protected void onPostExecute(BannerAuthResponse auth) {
    		dialog.dismiss();
    		
    		if (auth == null) {
    			if (serverError) {
    				Toast.makeText(LoginActivity.this, "Authentication service is rejecting requests.  Please try again later.", Toast.LENGTH_SHORT).show();
    			} else {
    				Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
    			}
    			return;
    		}
    		
    		processAuthentication(username, password, auth);
		}
    }

}
