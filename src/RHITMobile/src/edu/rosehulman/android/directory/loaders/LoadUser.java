package edu.rosehulman.android.directory.loaders;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.model.UserDataResponse;
import edu.rosehulman.android.directory.service.AuthenticationException;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

public class LoadUser extends CachedAsyncLoader<UserDataResponse> {

	private static final String ARG_AUTH_TOKEN = "AuthToken";
	private static final String ARG_USERNAME = "Username";
	
	public static Bundle bundleArgs(String authToken, String username) {
		Bundle res = new Bundle();
		res.putString(ARG_AUTH_TOKEN, authToken);
		res.putString(ARG_USERNAME, username);
		return res;
	}
	
	private String mAuthToken;
	private String mUsername;
	
	public LoadUser(Context context, Bundle args) {
		super(context);
		mAuthToken = args.getString(ARG_AUTH_TOKEN);
		mUsername = args.getString(ARG_USERNAME);
	}
	
	public String getAuthToken() {
		return mAuthToken;
	}

	@Override
	protected UserDataResponse doInBackground() throws AsyncLoaderException {

		MobileDirectoryService service = new MobileDirectoryService();
			
		while (true) {
			if (Thread.interrupted())
				return null;
			
			try {
				UserDataResponse response = service.getUser(mAuthToken, mUsername);
				
				if (response == null)
					return null;
				
				return response;

			} catch (AuthenticationException e) {
				Log.w(C.TAG, "Invalid auth token");
				throw new InvalidAuthTokenException();
				
			} catch (ClientException e) {
				Log.e(C.TAG, "Client request failed", e);
				throw new AsyncLoaderException(e.getMessage());
				
			} catch (ServerException e) {
				Log.e(C.TAG, "Server request failed", e);
				throw new AsyncLoaderException(getContext().getString(R.string.error_server));
				
			} catch (JSONException e) {
				Log.e(C.TAG, "An error occured while parsing the JSON response", e);
				throw new AsyncLoaderException(getContext().getString(R.string.error_json));
				
			} catch (IOException e) {
				Log.e(C.TAG, "Network error, retrying...", e);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					return null;
				}
			}
		}
	}

}
