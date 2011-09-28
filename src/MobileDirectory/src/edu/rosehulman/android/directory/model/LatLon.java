package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

public class LatLon {
	
	public int lat;
	public int lon;
	
	public LatLon() {
	}
	
	public LatLon(int lat, int lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public LatLon(double lat, double lon) {
		this.lat = (int)(lat*10E5);
		this.lon = (int)(lon*10E5);
	}
	
	public static LatLon deserialize(JSONObject root) throws JSONException {
		return new LatLon(root.getDouble("Lat"), root.getDouble("Long"));
	}
	
	public GeoPoint asGeoPoint() {
		return new GeoPoint(lat, lon);
	}

}
