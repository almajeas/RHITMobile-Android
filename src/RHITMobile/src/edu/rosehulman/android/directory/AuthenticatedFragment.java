package edu.rosehulman.android.directory;

import java.io.IOException;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;
import edu.rosehulman.android.directory.auth.AccountAuthenticator;

/**
 * A fragment that ensures that the user is logged in
 */
public class AuthenticatedFragment extends Fragment {
	
	public interface AuthenticationCallbacks {
		public void onAuthTokenObtained(String authtoken);
		public void onAuthTokenCancelled();
	}

	private String mAuthToken;
	private boolean mCancelled;
	
	//private static final int LOAD_AUTH_TOKEN = 400;
	
	public AuthenticatedFragment() {
		setRetainInstance(true);
		mCancelled = false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("Cancelled", mCancelled);
	}

	public boolean hasAuthToken() {
		return mAuthToken != null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Activity activity = getActivity();
		
		AccountManager manager = AccountManager.get(activity);

		if (!User.isLoggedIn(manager)) {
			activity.setResult(Activity.RESULT_CANCELED);
			activity.finish();
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Activity activity = getActivity();
		
		if (savedInstanceState != null) {
			mCancelled = savedInstanceState.getBoolean("Cancelled");
		}
		
		if (mCancelled) {
			activity.finish();
			return;
		}
	}
	
	private AccountManagerFuture<Bundle> startAuthRequest() {
		final Activity activity = getActivity();

		AccountManager manager = AccountManager.get(activity);
		
		return manager.getAuthToken(User.getAccount(manager), AccountAuthenticator.TOKEN_TYPE, null, activity, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				AuthenticationCallbacks callbacks = getCallbacks();
				
				try {
					Bundle res = future.getResult();
					if (!res.containsKey(AccountManager.KEY_AUTHTOKEN)) {
						callbacks.onAuthTokenCancelled();	
					} else {
						mAuthToken = res.getString(AccountManager.KEY_AUTHTOKEN);
						callbacks.onAuthTokenObtained(mAuthToken);
					}
					
				} catch (OperationCanceledException e) {
					Log.i(C.TAG, "User cancelled authentication");
					Toast.makeText(activity, "Unable to authenticate", Toast.LENGTH_SHORT).show();
					callbacks.onAuthTokenCancelled();
					
				} catch (AuthenticatorException e) {
					Log.e(C.TAG, "Authenticator error", e);
					Toast.makeText(activity, "Unable to authenticate", Toast.LENGTH_SHORT).show();
					callbacks.onAuthTokenCancelled();
				
				} catch (IOException e) {
					Log.e(C.TAG, "Network error, retrying...");
					try {
						Thread.sleep(2000);
						startAuthRequest();
					} catch (InterruptedException ex) {
					}
				}
			}
		}, null);
	}
	
	public void invalidateAuthToken(String authToken) {
		//getLoaderManager().destroyLoader(LOAD_AUTH_TOKEN);
		assert(authToken != null && authToken.equals(mAuthToken));
		AccountManager.get(getActivity()).invalidateAuthToken(AccountAuthenticator.ACCOUNT_TYPE, authToken);
		mAuthToken = null;
	}
	
	public AccountManagerFuture<Bundle> obtainAuthToken() {
		return startAuthRequest();
	}
	
	private AuthenticationCallbacks getCallbacks() {
		try {
			Activity activity = getActivity();
			if (activity == null) {
				return mLonelyCallbacks;
			} else {
				return (AuthenticationCallbacks)getActivity();
			}
		} catch (ClassCastException ex) {
			throw new RuntimeException("Activity must implement AuthenticationCallbacks");
		}
	}
	
	private AuthenticationCallbacks mLonelyCallbacks = new AuthenticationCallbacks() {
		
		@Override
		public void onAuthTokenObtained(String authtoken) {
			mAuthToken = authtoken;
		}
		
		@Override
		public void onAuthTokenCancelled() {
			mAuthToken = null;
			mCancelled = true;
		}
	};
}
