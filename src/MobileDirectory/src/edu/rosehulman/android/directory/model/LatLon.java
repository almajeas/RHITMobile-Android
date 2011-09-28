package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

public class LatLon {
	
	public int lat;
	public int lon;
	
	public LatLon(int lat, int lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public LatLon(double lat, double lon) {
		this.lat = (int)(lat*1E6);
		this.lon = (int)(lon*1E6);
	}
	
	public static LatLon deserialize(JSONObject root) throws JSONException {
		return new LatLon(root.getDouble("Lat"), root.getDouble("Long"));
	}
	
	public GeoPoint asGeoPoint() {
		return new GeoPoint(lat, lon);
	}

}
