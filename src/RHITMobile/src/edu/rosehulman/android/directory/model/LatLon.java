package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

/** Represents a Latitude/Longitude pair in microdegree form. */
public class LatLon implements Parcelable {
	
	/** Latitude represented in microdegrees */
	public int lat;
	
	/** Longitude represented in microdegrees */
	public int lon;
	
	/**
	 * Create a new LatLon from microdegree values
	 * 
	 * @param lat Latitude in microdegrees
	 * @param lon Longitude in microdegrees
	 */
	public LatLon(int lat, int lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	/**
	 * Create a new LatLon from degree values
	 * 
	 * @param lat Latitude in degrees
	 * @param lon Longitude in degrees
	 */
	public LatLon(double lat, double lon) {
		this.lat = (int)(lat*1E6);
		this.lon = (int)(lon*1E6);
	}
	
	@Override
	public LatLon clone() {
		return new LatLon(lat, lon);
	}
	
	/**
	 * Deserialize a JSON object into a LatLon instance
	 * 
	 * @param root The JSONObject that contains a Lat and Long field
	 * @return a new LatLon instance initialized from root
	 * @throws JSONException
	 */
	public static LatLon deserialize(JSONObject root) throws JSONException {
		return new LatLon(root.getDouble("Lat"), root.getDouble("Lon"));
	}
	
	/**
	 * Convert this LatLon instace to a GeoPoint for use with the Google Maps APIs
	 * 
	 * @return a new GeoPoint instance initialized from this LatLon instance
	 */
	public GeoPoint asGeoPoint() {
		return new GeoPoint(lat, lon);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(lat);
		dest.writeInt(lon);
	}
	
	public static final Parcelable.Creator<LatLon> CREATOR = new Parcelable.Creator<LatLon>() {

		@Override
		public LatLon createFromParcel(Parcel in) {
			int lat = in.readInt();
			int lon = in.readInt();
			
			return new LatLon(lat, lon);
		}

		@Override
		public LatLon[] newArray(int size) {
			return new LatLon[size];
		}
		
	};

}
