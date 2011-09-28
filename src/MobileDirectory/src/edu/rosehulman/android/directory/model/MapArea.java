package edu.rosehulman.android.directory.model;

public class MapArea {

	public int id;
	public String name;
	public String description;
	public boolean labelOnHybrid;
	public int minZoomLevel;
	public LatLon center;
	public LatLon corners[];
	
	public MapArea() {
	}
	
	public MapArea(String name, int latE6, int lonE6) {
		this.name = name;
		this.center = new LatLon(latE6, lonE6);
		this.labelOnHybrid = true;
	}
	
	public boolean hasCorners() {
		return corners != null;
	}

}
