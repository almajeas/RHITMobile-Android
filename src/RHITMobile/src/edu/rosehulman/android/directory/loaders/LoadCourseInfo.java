package edu.rosehulman.android.directory.loaders;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.model.Course;
import edu.rosehulman.android.directory.service.AuthenticationException;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

public class LoadCourseInfo extends CachedAsyncLoader<Course> {
	
	private static final String ARG_AUTH_TOKEN = "AuthToken";
	private static final String ARG_TERM = "Term";
	private static final String ARG_CRN = "CRN";
	
	public static Bundle bundleArgs(String authToken, String term, int crn) {
		Bundle res = new Bundle();
		res.putString(ARG_AUTH_TOKEN, authToken);
		res.putString(ARG_TERM, term);
		res.putInt(ARG_CRN, crn);
		return res;
	}
	
	public static LoadCourseInfo getInstance(LoaderManager loaderManager, int id) {
		Loader<LoaderResult<Course>> res = loaderManager.getLoader(id);
		return (LoadCourseInfo)res;
	}

	private String mAuthToken;
	private String mTerm;
	private int mCRN;
	
	public LoadCourseInfo(Context context, Bundle args) {
		super(context);
		mAuthToken = args.getString(ARG_AUTH_TOKEN);
		mTerm = args.getString(ARG_TERM);
		mCRN = args.getInt(ARG_CRN);
	}

	public String getAuthToken() {
		return mAuthToken;
	}
	
	@Override
	protected Course doInBackground() throws LoaderException {
		Log.d(C.TAG, "Starting LoadCourseInfo");
		
		MobileDirectoryService service = new MobileDirectoryService();
		
		while (true) {
			if (Thread.interrupted())
				return null;
			
			try {
				return service.getCourse(mAuthToken, mTerm, mCRN);

			} catch (AuthenticationException e) {
				Log.w(C.TAG, "Invalid auth token");
				throw new InvalidAuthTokenException();
				
			} catch (ClientException e) {
				Log.e(C.TAG, "Client request failed", e);
				throw new LoaderException(e.getMessage());
				
			} catch (ServerException e) {
				Log.e(C.TAG, "Server request failed", e);
				throw new LoaderException(getContext().getString(R.string.error_server));
				
			} catch (JSONException e) {
				Log.e(C.TAG, "An error occured while parsing the JSON response", e);
				throw new LoaderException(getContext().getString(R.string.error_json));
				
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
