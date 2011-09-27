package edu.rosehulman.android.directory.model;

import com.google.android.maps.GeoPoint;

public class Building {

	public int id;
	public String name;
	public String description;
	public boolean showLabel;
	public int centerLat;
	public int centerLon;
	
	public Building(String name, int latE6, int lonE6) {
		this.name = name;
		this.centerLat = latE6;
		this.centerLon = lonE6;
		this.showLabel = true;
	}

	public GeoPoint getCenter() {
		return new GeoPoint(centerLat, centerLon);
	}

}
