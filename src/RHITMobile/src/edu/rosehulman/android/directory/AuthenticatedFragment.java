package edu.rosehulman.android.directory;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import edu.rosehulman.android.directory.auth.AccountAuthenticator;

/**
 * A fragment that ensures that the user is logged in
 */
public class AuthenticatedFragment extends Fragment {

	private static final int REQUEST_AUTHENTICATE = 12543;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Context context = getActivity();

		AccountManager manager = AccountManager.get(context);

		if (!User.isLoggedIn(manager)) {
			manager.addAccount(AccountAuthenticator.ACCOUNT_TYPE, AccountAuthenticator.TOKEN_TYPE, null, null, null, new AccountManagerCallback<Bundle>() {
				@Override
				public void run(AccountManagerFuture<Bundle> future) {
					try {
						Intent intent = future.getResult().getParcelable(AccountManager.KEY_INTENT);
						startActivityForResult(intent, REQUEST_AUTHENTICATE);
						
					} catch (Exception e) {
						Activity activity = getActivity();
						activity.setResult(Activity.RESULT_CANCELED);
						activity.finish();
					}
				}
			}, null);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Activity activity = getActivity();

		if (requestCode == REQUEST_AUTHENTICATE) {
			if (resultCode == Activity.RESULT_CANCELED || !User.isLoggedIn(AccountManager.get(activity))) {
				activity.setResult(Activity.RESULT_CANCELED);
				activity.finish();
			}
		}
	}
}
