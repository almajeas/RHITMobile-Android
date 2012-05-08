package edu.rosehulman.android.directory.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.Toast;
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.loaders.GetUserLocation;
import edu.rosehulman.android.directory.loaders.GpsDisabledException;
import edu.rosehulman.android.directory.loaders.LoaderException;
import edu.rosehulman.android.directory.loaders.LoaderResult;
import edu.rosehulman.android.directory.loaders.NoGpsAvailableException;
import edu.rosehulman.android.directory.model.LatLon;

public class ObtainLocationDialogFragment extends DialogFragment {
	
	public static final String TAG = "ObtainLocationDialog";

	public interface LocationCallbacks {
		public void onLocationObtained(LatLon loc);
		public void onLocationCancelled();
	}

	private static final int LOAD_USER_LOCATION = 1;
	
	private LocationCallbacks mCallbacks;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mCallbacks = (LocationCallbacks)activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement " + LocationCallbacks.class.getName());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		mCallbacks = null;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		loadLocation();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setTitle(null);
		dialog.setMessage("Finding location...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		
		return dialog;
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		
		getLoaderManager().destroyLoader(LOAD_USER_LOCATION);
	}

	private void loadLocation() {
		getLoaderManager().initLoader(LOAD_USER_LOCATION, null, mLoadUserLocationCallbacks);
	}

	private LoaderManager.LoaderCallbacks<LoaderResult<LatLon>> mLoadUserLocationCallbacks = new LoaderManager.LoaderCallbacks<LoaderResult<LatLon>>() {
		
		private Handler mHandler = new Handler();
		
		@Override
		public Loader<LoaderResult<LatLon>> onCreateLoader(int id, Bundle args) {
			return new GetUserLocation(getActivity());
		}

		@Override
		public void onLoadFinished(Loader<LoaderResult<LatLon>> loader, LoaderResult<LatLon> data) {
			try {
				final LatLon res = data.getResult();
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mCallbacks.onLocationObtained(res);
						dismiss();		
					}
				});
				
			} catch (GpsDisabledException e) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						new EnableGpsDialogFragment().show(getFragmentManager(), EnableGpsDialogFragment.TAG);		
					}
				});
				
			} catch (NoGpsAvailableException e) {
				Toast.makeText(getActivity(), R.string.no_gps_available, Toast.LENGTH_SHORT);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						dismiss();		
					}
				});
				
			} catch (LoaderException e) {
				throw new RuntimeException("Unexpected error in loader", e);
			}
		}

		@Override
		public void onLoaderReset(Loader<LoaderResult<LatLon>> loader) {
		}
	};

}
