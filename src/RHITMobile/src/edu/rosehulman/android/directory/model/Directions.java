package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Directions {
	
	public double distance;
	
	public LatLon start;
	
	public int stairsUp;
	
	public int stairsDown;
	
	public Path[] paths;

	private static Path[] deserializePaths(JSONArray root) throws JSONException {
		Path[] res = new Path[root.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = Path.deserialize(root.getJSONObject(i));
		}
		
		return res;
	}
	
	public static Directions deserialize(JSONObject root) throws JSONException {
		Directions res = new Directions();
		
		res.distance = root.getDouble("Dist");
		res.start = LatLon.deserialize(root.getJSONObject("Start"));
		res.stairsUp = root.getInt("StairsUp");
		res.stairsDown = root.getInt("StairsDown");
		res.paths = deserializePaths(root.getJSONArray("Paths"));

		return res;
	}

}
