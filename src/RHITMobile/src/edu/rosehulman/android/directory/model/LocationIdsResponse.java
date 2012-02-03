package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an ordered list of location ids
 */
public class LocationIdsResponse {

	/**
	 * The listing of ids
	 */
	public long[] ids;
	
	public static long[] deserializeIds(JSONArray array) throws JSONException {
		long[] ids = new long[array.length()];
		
		for (int i = 0; i < ids.length; i++) {
			ids[i] = array.getLong(i);
		}
		
		return ids;
	}
	
	/**
	 * Deserialize a new instance from a JSON object
	 * 
	 * @param root The JSON object in the proper form
	 * @return A new LocationIdsResponse
	 * @throws JSONException On Error
	 */
	public static LocationIdsResponse deserialize(JSONObject root) throws JSONException {
		LocationIdsResponse res = new LocationIdsResponse();
		
		res.ids = deserializeIds(root.getJSONArray("Locations"));
		
		return res;
	}
	
}
