package edu.rosehulman.android.directory.loaders;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.model.LatLon;

public class GetUserLocation extends SyncLoader<LatLon> {
	
	private LocationManager mLocationManager;
	private UserLocationListener mLocationListener;

	public GetUserLocation(Context context) {
		super(context);
	}
	
	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		
		if (mLocationManager != null && mLocationListener != null) {
			mLocationManager.removeUpdates(mLocationListener);
			mLocationManager = null;
			mLocationListener = null;
		}
	}
	
	@Override
	protected void onReset() {
		super.onReset();
		
		mLocationManager = null;
		mLocationListener = null;
	}

	@Override
	protected void loadData() throws LoaderException {
		mLocationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new UserLocationListener(mLocationManager);
		
		if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) == null) {
			throw new NoGpsAvailableException();
		}
		
		if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			throw new GpsDisabledException();
		}
		
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
	}
	
	private class UserLocationListener implements LocationListener {
		
		private LocationManager mManager;
		private int mFixCount;

		private Location mCurrentLocation;
		
		public UserLocationListener(LocationManager manager) {
			mManager = manager;
		}
		
		@Override
		public void onLocationChanged(Location location) {
			Log.v(C.TAG, location.toString());
			mFixCount++;
			if (isBetterLocation(location, mCurrentLocation)) {
				mCurrentLocation = location;
			}
			
			if (mFixCount > 6 || (mCurrentLocation.hasAccuracy() && mCurrentLocation.getAccuracy() <= 11.0f)) {
				handleResult(new LatLon(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
				mManager.removeUpdates(this);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			handleError(new GpsDisabledException());
			mManager.removeUpdates(this);
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
		private static final int MAX_LOCATION_AGE = 1000 * 60 * 1;

		/** Determines whether one Location reading is better than the current Location fix
		  * @param location  The new Location that you want to evaluate
		  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
		  */
		protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		    if (currentBestLocation == null) {
		        // A new location is always better than no location
		        return true;
		    }

		    // Check whether the new location fix is newer or older
		    long timeDelta = location.getTime() - currentBestLocation.getTime();
		    boolean isSignificantlyNewer = timeDelta > MAX_LOCATION_AGE;
		    boolean isSignificantlyOlder = timeDelta < -MAX_LOCATION_AGE;
		    boolean isNewer = timeDelta > 0;

		    // If it's been more than MAX_LOCATION_AGE since the current location, use the new location
		    // because the user has likely moved
		    if (isSignificantlyNewer) {
		        return true;
		    // If the new location is more than MAX_LOCATION_AGE older, it must be worse
		    } else if (isSignificantlyOlder) {
		        return false;
		    }

		    // Check whether the new location fix is more or less accurate
		    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		    boolean isLessAccurate = accuracyDelta > 0;
		    boolean isMoreAccurate = accuracyDelta < 0;
		    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		    // Determine location quality using a combination of timeliness and accuracy
		    if (isMoreAccurate) {
		        return true;
		    } else if (isNewer && !isLessAccurate) {
		        return true;
		    } else if (isNewer && !isSignificantlyLessAccurate) {
		        return true;
		    }
		    return false;
		}
	}
}
