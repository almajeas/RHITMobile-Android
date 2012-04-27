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
import android.support.v4.content.Loader;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.User;
import edu.rosehulman.android.directory.auth.AccountAuthenticator;

public class LoadAuthToken extends Loader<String> {
	
	private Activity mActivity;
	private AccountManagerFuture<Bundle> mPendingRequest;
	
	private String mAuthToken;

	public LoadAuthToken(Activity activity) {
		super(activity);
		mActivity = activity;
	}
	
	public void setActivity(Activity activity) {
		mActivity = activity;
	}
	
	@Override
	protected void onStartLoading() {
		if (mAuthToken == null) {
			forceLoad();
			
		} else {
			deliverResult(mAuthToken);	
		}
	}
	
	@Override
	protected void onForceLoad() {
		if (mActivity == null) {
			deliverResult(null);
			return;
		}

		AccountManager manager = AccountManager.get(mActivity);

		final Handler handler = new Handler();
		mPendingRequest = manager.getAuthToken(User.getAccount(manager), AccountAuthenticator.TOKEN_TYPE, null, mActivity, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				
				try {
					Bundle res = future.getResult();
					if (!res.containsKey(AccountManager.KEY_AUTHTOKEN)) {
						deliverResult(null);	
					} else {
						mAuthToken = res.getString(AccountManager.KEY_AUTHTOKEN);
						deliverResult(mAuthToken);
					}
					
				} catch (OperationCanceledException e) {
					Log.i(C.TAG, "User cancelled authentication");
					//Toast.makeText(mActivity, "Unable to authenticate", Toast.LENGTH_SHORT).show();
					deliverResult(null);
					
				} catch (AuthenticatorException e) {
					Log.e(C.TAG, "Authenticator error", e);
					//Toast.makeText(mActivity, "Unable to authenticate", Toast.LENGTH_SHORT).show();
					deliverResult(null);
				
				} catch (IOException e) {
					Log.e(C.TAG, "Network error, retrying...");
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							onForceLoad();
						}
					}, 2000);
				}
			}
		}, handler);
	}

	@Override
	protected void onStopLoading() {
		if (mPendingRequest != null) {
			mPendingRequest.cancel(true);
			mPendingRequest = null;
		}
	}
	
	@Override
	protected void onReset() {
		onStopLoading();
		
		mAuthToken = null;
	}
}
