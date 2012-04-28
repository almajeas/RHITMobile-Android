package edu.rosehulman.android.directory.loaders;

import java.io.IOException;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.User;
import edu.rosehulman.android.directory.auth.AccountAuthenticator;

public class LoadAuthToken extends Loader<String> {
	
	private static final String ARG_ABORT_ON_LOGIN = "AbortOnLogin";
	
	public static Bundle bundleArgs(boolean abortOnLogin) {
		Bundle args = new Bundle();
		args.putBoolean(ARG_ABORT_ON_LOGIN, abortOnLogin);
		return args;
	}
	
	public static LoadAuthToken getInstance(LoaderManager manager, int id) {
		Loader<String> loader = manager.getLoader(id);
		return (LoadAuthToken)loader;
	}
	
	private boolean mAbortOnLogin;
	
	private boolean mLoading;
	private boolean mLoaded;
	
	private Activity mActivity;
	
	private String mAuthToken;

	public LoadAuthToken(Bundle args, Activity activity) {
		super(activity);
		mAbortOnLogin = args.getBoolean(ARG_ABORT_ON_LOGIN);
		mActivity = activity;
	}
	
	public void setActivity(Activity activity) {
		mActivity = activity;
	}
	
	@Override
	protected void onStartLoading() {
		if (mLoaded) {
			deliverResult(mAuthToken);
			
		} else if (!mLoading) {
			forceLoad();
		}
	}

	@Override
	protected void onForceLoad() {
		mLoading = true;
		
		loadData();
	}

	@Override
	protected void onStopLoading() {
		if (!mLoading)
			return;
		
		mLoading = false;
	}
	
	@Override
	protected void onReset() {
		onStopLoading();
		
		mAuthToken = null;
		mLoaded = false;
	}
	
	@Override
	public void deliverResult(String data) {
		mLoaded = true;
		mLoading = false;
		mAuthToken = data;
		super.deliverResult(data);
	}
	
	private void loadData() {
		
		if (mActivity == null) {
			deliverResult(null);
			return;
		}

		AccountManager manager = AccountManager.get(mActivity);
		
		final Handler handler = new Handler();
		
		Activity activeActivity = mActivity;
		if (mAbortOnLogin)
			activeActivity = null;
		
		manager.getAuthToken(User.getAccount(manager), AccountAuthenticator.TOKEN_TYPE, null, activeActivity, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				
				try {
					Bundle res = future.getResult();
					if (!res.containsKey(AccountManager.KEY_AUTHTOKEN)) {
						deliverResult(null);
					} else {
						String authToken = res.getString(AccountManager.KEY_AUTHTOKEN);
						deliverResult(authToken);
					}
					
				} catch (OperationCanceledException e) {
					Log.i(C.TAG, "User cancelled authentication");
					deliverResult(null);
					
				} catch (AuthenticatorException e) {
					Log.e(C.TAG, "Authenticator error", e);
					deliverResult(null);
				
				} catch (IOException e) {
					Log.e(C.TAG, "Network error, retrying...");
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							loadData();
						}
					}, 2000);
				}
			}
		}, handler);
	}
}
