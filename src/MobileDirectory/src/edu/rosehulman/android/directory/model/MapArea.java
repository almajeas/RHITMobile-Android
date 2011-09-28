package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	
	private static LatLon[] deserializeCorners(JSONArray array) throws JSONException {
		LatLon[] res = new LatLon[array.length()];
		for (int i = 0; i < res.length; i++) {
			res[i] = LatLon.deserialize(array.getJSONObject(i));
		}
		return res;
	}
	
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
