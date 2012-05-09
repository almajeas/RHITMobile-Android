package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an id of a single location
 */
public class LocationIdResponse {
	
	/** The single id */
	public long id;
	
	/**
	 * Deserialize the given JSONObject into a new instance
	 * 
	 * @param root The JSONObject with the necessary field to create a new instance
	 * @return A new instance initialized from the given JSONObject
	 * @throws JSONException On error
	 */
	public static LocationIdResponse deserialize(JSONObject root) throws JSONException {
		LocationIdResponse res = new LocationIdResponse();
		
		res.id = root.getLong("Id");
		
		return res;
	}

}
