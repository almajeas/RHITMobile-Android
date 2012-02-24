package edu.rosehulman.android.directory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * A fragment that ensures that the user is logged in
 */
public class AuthenticatedFragment extends Fragment {

	private static final int REQUEST_AUTHENTICATE = 12543;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Context context = getActivity();

		if (!User.isLoggedIn()) {
			startActivityForResult(LoginActivity.createIntent(context), REQUEST_AUTHENTICATE);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Activity activity = getActivity();

		if (requestCode == REQUEST_AUTHENTICATE) {
			if (resultCode == Activity.RESULT_CANCELED || !User.isLoggedIn()) {
				activity.setResult(Activity.RESULT_CANCELED);
				activity.finish();
			}
		}
	}
}
