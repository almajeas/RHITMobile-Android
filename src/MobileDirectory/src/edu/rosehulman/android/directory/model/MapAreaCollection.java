package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapAreaCollection {
	
	/** Collection of MapArea objects */
	public MapArea mapAreas[];
	
	/** Version associated with the given collection of MapArea objects */
	public String version;
	
	private static MapArea[] deserializeMapAreas(JSONArray array) throws JSONException {
		MapArea res[] = new MapArea[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = MapArea.deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
	
	/**
	 * Deserialize the given JSONObject into a new instance of MapAreaCollection
	 * 
	 * @param root The JSONObject with the necessary field to create a new MapAreaCollection
	 * @return A new MapAreaCollection initialized from the given JSONObject
	 * @throws JSONException
	 */
	public static MapAreaCollection deserialize(JSONObject root) throws JSONException {
		MapAreaCollection res = new MapAreaCollection();
		
		res.version = root.getString("Version");
		res.mapAreas = deserializeMapAreas(root.getJSONArray("Areas"));
		
		return res;
	}

}
