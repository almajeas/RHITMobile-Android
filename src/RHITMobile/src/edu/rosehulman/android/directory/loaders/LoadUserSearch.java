package edu.rosehulman.android.directory.loaders;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.model.ShortUser;
import edu.rosehulman.android.directory.model.UsersResponse;
import edu.rosehulman.android.directory.service.AuthenticationException;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

public class LoadUserSearch extends CachedAsyncLoader<ShortUser[]> {

	private static final String ARG_AUTH_TOKEN = "AuthToken";
	private static final String ARG_SEARCH = "Search";
	
	public static Bundle bundleArgs(String authToken, String search) {
		Bundle res = new Bundle();
		res.putString(ARG_AUTH_TOKEN, authToken);
		res.putString(ARG_SEARCH, search);
		return res;
	}
	
	private String mAuthToken;
	private String mSearch;
	
	public LoadUserSearch(Context context, Bundle args) {
		super(context);
		mAuthToken = args.getString(ARG_AUTH_TOKEN);
		mSearch = args.getString(ARG_SEARCH);
	}

	@Override
	protected ShortUser[] doInBackground() throws AsyncLoaderException {

		MobileDirectoryService service = new MobileDirectoryService();
			
		while (true) {
			if (Thread.interrupted())
				return null;
			
			try {
				UsersResponse response = service.searchUsers(mAuthToken, mSearch);
				
				if (response == null)
					return null;
				
				return response.users;
				
	
			} catch (AuthenticationException e) {
				Log.w(C.TAG, "Invalid auth token");
				throw new InvalidAuthTokenException();
				
			} catch (ClientException e) {
				Log.e(C.TAG, "Client request failed", e);
				throw new AsyncLoaderException(getContext().getString(R.string.error_client));
				
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
