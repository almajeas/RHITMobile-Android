package edu.rosehulman.android.directory.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import edu.rosehulman.android.directory.R;

public class EnableGpsDialogFragment extends DialogFragment {
	
	public static final String TAG = "EnableGpsDialog";
	
	public interface EnableGpsCallbacks {
		public void onEnableGpsTriggered();
	}
	
	private EnableGpsCallbacks mCallbacks;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mCallbacks = (EnableGpsCallbacks)activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement " + EnableGpsCallbacks.class.getName());
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		mCallbacks = null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
    	.setTitle(R.string.location_services_title)
    	.setMessage(R.string.location_services_message)
    	.setPositiveButton(R.string.do_it, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
				mCallbacks.onEnableGpsTriggered();
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onCancel(dialog);
			}
		}).create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		mCallbacks.onEnableGpsTriggered();
	}
}
