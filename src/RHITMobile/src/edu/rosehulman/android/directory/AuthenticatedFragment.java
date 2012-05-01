package edu.rosehulman.android.directory;

import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import edu.rosehulman.android.directory.auth.AccountAuthenticator;
import edu.rosehulman.android.directory.loaders.LoadAuthToken;

/**
 * A fragment that ensures that the user is logged in
 */
public class AuthenticatedFragment extends Fragment {
	
	private static final String KEY_LOAD_ATTEMPTED = "LoadAttempted";
	
	public interface AuthenticationCallbacks {
		public void onAuthTokenObtained(String authToken);
		public void onAuthTokenCancelled();
	}

	private boolean mLoadAttempted;
	private boolean mAbortOnLogin;
	
	private String mAuthToken;
	
	private static final int LOAD_AUTH_TOKEN = 400;
	
	public AuthenticatedFragment() {
		setRetainInstance(true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(KEY_LOAD_ATTEMPTED, mLoadAttempted);
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
		
		LoaderManager loaders = getLoaderManager();
		if (loaders.getLoader(LOAD_AUTH_TOKEN) != null) {
			loaders.initLoader(LOAD_AUTH_TOKEN, null, mLoadAuthTokenCallbacks);
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			mLoadAttempted = savedInstanceState.getBoolean(KEY_LOAD_ATTEMPTED);
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		LoadAuthToken loader = LoadAuthToken.getInstance(getLoaderManager(), LOAD_AUTH_TOKEN);
		if (loader != null) {
			loader.setActivity(null);
		}
	}

	public void invalidateAuthToken(String authToken) {
		assert(authToken != null && authToken.equals(mAuthToken));
		AccountManager.get(getActivity()).invalidateAuthToken(AccountAuthenticator.ACCOUNT_TYPE, authToken);
		
		getLoaderManager().destroyLoader(LOAD_AUTH_TOKEN);
		mAuthToken = null;
	}
	
	public void obtainAuthToken() {
		if (mLoadAttempted) {
			mAbortOnLogin = true;
		}
		
		mLoadAttempted = true;
		loadAuthToken();
	}
	
	private AuthenticationCallbacks getCallbacks() {
		Activity activity = getActivity();
		try {
			if (activity == null) {
				return mLonelyCallbacks;
			} else {
				return (AuthenticationCallbacks)getActivity();
			}
		} catch (ClassCastException ex) {
			throw new ClassCastException(activity.toString() + " must implement AuthenticationCallbacks");
		}
	}
	
	private AuthenticationCallbacks mLonelyCallbacks = new AuthenticationCallbacks() {
		
		@Override
		public void onAuthTokenObtained(String authToken) {
			mAuthToken = authToken;
		}
		
		@Override
		public void onAuthTokenCancelled() {
			mAuthToken = null;
		}
	};

	private void loadAuthToken() {
		LoaderManager loaderManager = getLoaderManager();
		
		LoadAuthToken loader;
		Bundle args = LoadAuthToken.bundleArgs(mAbortOnLogin);
		loader = (LoadAuthToken)loaderManager.restartLoader(LOAD_AUTH_TOKEN, args, mLoadAuthTokenCallbacks);
		loader.setActivity(getActivity());
	}
	
	private LoaderManager.LoaderCallbacks<String> mLoadAuthTokenCallbacks = new LoaderManager.LoaderCallbacks<String>() {
		@Override
		public Loader<String> onCreateLoader(int id, Bundle args) {
			return new LoadAuthToken(args, getActivity());
		}

		@Override
		public void onLoadFinished(Loader<String> loader, String data) {
			mAuthToken = data;
			
			AuthenticationCallbacks callbacks = getCallbacks();
			
			if (mAuthToken == null) {
				callbacks.onAuthTokenCancelled();
			} else {
				mLoadAttempted = false;
				callbacks.onAuthTokenObtained(mAuthToken);
			}
		}

		@Override
		public void onLoaderReset(Loader<String> loader) {
			mAuthToken = null;
		}
	};
}
