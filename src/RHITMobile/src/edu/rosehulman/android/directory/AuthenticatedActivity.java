package edu.rosehulman.android.directory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * An activity that ensures that the user is logged in
 */
public class AuthenticatedActivity extends Activity {

	private static final int REQUEST_AUTHENTICATE = 12543;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!User.isLoggedIn()) {
			startActivityForResult(LoginActivity.createIntent(this), REQUEST_AUTHENTICATE);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_AUTHENTICATE) {
			if (resultCode == RESULT_CANCELED || !User.isLoggedIn()) {
				setResult(RESULT_CANCELED);
				finish();
			}
		}
	}
}
