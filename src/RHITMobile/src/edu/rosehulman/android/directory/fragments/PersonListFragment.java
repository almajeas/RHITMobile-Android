package edu.rosehulman.android.directory.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

import edu.rosehulman.android.directory.PersonActivity;
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.loaders.InvalidAuthTokenException;
import edu.rosehulman.android.directory.loaders.LoadUserSearch;
import edu.rosehulman.android.directory.loaders.LoaderException;
import edu.rosehulman.android.directory.loaders.LoaderResult;
import edu.rosehulman.android.directory.model.ShortUser;

public class PersonListFragment extends SherlockListFragment {
	
	public interface PersonListCallbacks {
		public void onInvalidateAuthToken(String authToken);
		public void onRequestAuthToken();
	}
	
	private static final int LOAD_USER_SEARCH = 1;

	private PersonListCallbacks mCallbacks;
	private String mAuthToken;

	private String mSearchQuery;
	private ShortUser[] mUsers;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mCallbacks = (PersonListCallbacks)activity;			
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement " + PersonListCallbacks.class.getName());
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Activity activity = getActivity();

		setEmptyText(activity.getString(R.string.no_results));

		Intent intent = activity.getIntent();

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			runSearch(intent.getStringExtra(SearchManager.QUERY));

		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri data = intent.getData();

			activity.finish();
			Intent newIntent = PersonActivity.createIntent(getActivity(), data.getPath());
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(newIntent);

		} else {
			//are you lost?
			activity.finish();
			return;
		}
	}
	
	public void onAuthTokenObtained(String authToken) {
		mAuthToken = authToken;
		loadUsers();
	}

	private void runSearch(String query) {
		mSearchQuery = query;
		
		SherlockFragmentActivity activity = getSherlockActivity();
		activity.getSupportActionBar().setSubtitle(mSearchQuery);
		activity.setProgressBarIndeterminateVisibility(true);
		
		mCallbacks.onRequestAuthToken();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long rowId)
	{
		getActivity().finish();
		Intent newIntent = PersonActivity.createIntent(getActivity(), mUsers[position].username);
		startActivity(newIntent);
	}

	private void processResult(ShortUser[] users) {
		mUsers = users;

		ArrayAdapter<ShortUser> adapter = null;
		if (mUsers != null) {
			adapter = new ArrayAdapter<ShortUser>(getActivity(),
					R.layout.search_item, R.id.name, users) {
	
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View v = convertView;					
					if (v == null) {
						LayoutInflater inflater = LayoutInflater.from(getActivity());
						v = inflater.inflate(R.layout.search_item, null);
					}

					TextView name = (TextView)v.findViewById(R.id.name);
					TextView info = (TextView)v.findViewById(R.id.description);

					name.setText(mUsers[position].fullname);
					info.setText(mUsers[position].subtitle);

					return v;
				}
			};
		}
		setListAdapter(adapter);
	}
	
	private Bundle mArgs;
	private void loadUsers() {
		Bundle args = LoadUserSearch.bundleArgs(mAuthToken, mSearchQuery);
		
		if (mArgs != null) {
			getLoaderManager().restartLoader(LOAD_USER_SEARCH, args, mLoadUsersCallbacks);
		} else {
			getLoaderManager().initLoader(LOAD_USER_SEARCH, args, mLoadUsersCallbacks);
		}
		
		mArgs = args;
	}
	
	private LoaderCallbacks<LoaderResult<ShortUser[]>> mLoadUsersCallbacks = new LoaderCallbacks<LoaderResult<ShortUser[]>>() {
		
		@Override
		public Loader<LoaderResult<ShortUser[]>> onCreateLoader(int id, Bundle args) {
			return new LoadUserSearch(getActivity(), args);
		}

		@Override
		public void onLoadFinished(Loader<LoaderResult<ShortUser[]>> loader, LoaderResult<ShortUser[]> data) {
			
			try {
				final ShortUser[] result = data.getResult();
				getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
				processResult(result);
				
			} catch (InvalidAuthTokenException ex) {
				mCallbacks.onInvalidateAuthToken(mAuthToken);
				mCallbacks.onRequestAuthToken();
				mAuthToken = null;
				
			} catch (LoaderException ex) {
				String message = ex.getMessage();
				if (message != null) {
					Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				}
				getActivity().finish();
			}	
		}

		@Override
		public void onLoaderReset(Loader<LoaderResult<ShortUser[]>> loader) {
			processResult(null);
		}
	};
}
