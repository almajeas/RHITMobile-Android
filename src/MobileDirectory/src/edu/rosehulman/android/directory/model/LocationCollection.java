package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationCollection {
	
	/** Collection of Location objects */
	public Location mapAreas[];
	
	/** Version associated with the given collection of MapArea objects */
	public String version;
	
	private static Location[] deserializeMapAreas(JSONArray array) throws JSONException {
		Location res[] = new Location[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = Location.deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
	
	/**
	 * Deserialize the given JSONObject into a new instance of LocationCollection
	 * 
	 * @param root The JSONObject with the necessary field to create a new MapAreaCollection
	 * @return A new MapAreaCollection initialized from the given JSONObject
	 * @throws JSONException
	 */
	public static LocationCollection deserialize(JSONObject root) throws JSONException {
		LocationCollection res = new LocationCollection();
		
		res.version = root.getString("Version");
		res.mapAreas = deserializeMapAreas(root.getJSONArray("Locations"));
		
		return res;
	}

}
