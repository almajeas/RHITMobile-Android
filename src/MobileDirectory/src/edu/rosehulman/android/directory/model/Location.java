package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Location {

	/** Unique identifier for this Location */
	public long id;
	
	/** Parent identifier to this Location */
	public long parentId;

	/** The name of the area this Location represents */
	public String name;

	/** A short description of the Location */
	public String description;

	/** The center point of this Location */
	public LatLon center;
	
	/** Is the location a point of interest? */
	public boolean isPOI;
	
	/** Is the location on the quick list? */
	public boolean isOnQuickList;
	
	/** The id of the map area (or -1 if unavailable) */
	public long mapAreaId;
	
	/** The map area data associated with this location */
	public MapAreaData mapData;

	/** Create a new Location with all fields set to their default values */
	public Location() {
	}

	/**
	 * Create a new Location with some common parameters
	 * 
	 * @param name The name of the Location
	 * @param latE6 The latitude of the center of the area, in microdegrees
	 * @param lonE6 The longitude of the center of the area, in microdegrees
	 */
	public Location(String name, int latE6, int lonE6) {
		this.name = name;
		this.center = new LatLon(latE6, lonE6);
	}
	
	/**
	 * Deserialize the given JSONObject into an instance of Location
	 * 
	 * @param root The JSONObject with the required fields to create a new, 
	 * fully initialized MapArea
	 * @return A new Location
	 * @throws JSONException
	 */
	public static Location deserialize(JSONObject root) throws JSONException {
		Location res = new Location();
		
		res.id = root.getInt("Id");
		if (root.isNull("Parent"))
			res.parentId = -1;
		else
			res.parentId = root.getInt("Parent");
		res.name = root.getString("Name");
		res.description = root.getString("Description");
		res.isPOI = root.getBoolean("IsPOI");
		res.isOnQuickList = root.getBoolean("OnQuickList");
		res.center = LatLon.deserialize(root.getJSONObject("Center"));
		if (!root.isNull("MapArea")) {
			res.mapData = MapAreaData.deserialize(root.getJSONObject("MapArea"));
		}
		
		return res;
	}

}
