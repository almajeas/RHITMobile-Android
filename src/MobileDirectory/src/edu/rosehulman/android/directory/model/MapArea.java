package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapArea {

	/** Unique identifier for this MapArea */
	public int id;

	/** The name of the area this MapArea represents */
	public String name;

	/** A short description of the MapArea */
	public String description;

	/** True if this mapArea is labeled on Google's Hybrid map layer */
	public boolean labelOnHybrid;

	/** The minimum zoom level that this mapview should be displayed at */
	public int minZoomLevel;

	/** The center point of this MapArea */
	public LatLon center;

	/** A set of coordinates providing an enclosing polygon around this area */
	public LatLon corners[];

	/** Create a new MapArea with all fields set to their default values */
	public MapArea() {
	}

	/**
	 * Create a new MapArea with some common parameters
	 * 
	 * @param name The name of the MapArea
	 * @param latE6 The latitude of the center of the area, in microdegrees
	 * @param lonE6 The longitude of the center of the area, in microdegrees
	 */
	public MapArea(String name, int latE6, int lonE6) {
		this.name = name;
		this.center = new LatLon(latE6, lonE6);
		this.labelOnHybrid = true;
	}
	
	/**
	 * Determine if the MapArea has corners available
	 * 
	 * @return True if the MapArea has corners available
	 */
	public boolean hasCorners() {
		return corners != null;
	}
	
	private static LatLon[] deserializeCorners(JSONArray array) throws JSONException {
		LatLon[] res = new LatLon[array.length()];
		for (int i = 0; i < res.length; i++) {
			res[i] = LatLon.deserialize(array.getJSONObject(i));
		}
		return res;
	}
	
	/**
	 * Deserialize the given JSONObject into an instance of MapArea
	 * 
	 * @param root The JSONObject with the required fields to create a new, 
	 * fully initialized MapArea
	 * @return A new MapArea
	 * @throws JSONException
	 */
	public static MapArea deserialize(JSONObject root) throws JSONException {
		MapArea res = new MapArea();
		
		res.id = root.getInt("Id");
		res.name = root.getString("Name");
		res.description = root.getString("Description");
		res.labelOnHybrid = root.getBoolean("LabelOnHybrid");
		res.minZoomLevel = root.getInt("MinZoomLevel");
		res.corners = deserializeCorners(root.getJSONArray("Corners"));
		res.center = LatLon.deserialize(root.getJSONObject("Center"));
		
		return res;
	}

}
