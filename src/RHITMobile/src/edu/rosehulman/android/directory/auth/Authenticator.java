package edu.rosehulman.android.directory.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class Authenticator extends Service {

	private AccountAuthenticator mAuth;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mAuth = new AccountAuthenticator(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mAuth.getIBinder();
	}

}
