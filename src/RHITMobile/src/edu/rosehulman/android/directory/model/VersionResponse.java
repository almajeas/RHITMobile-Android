package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains current version information
 */
public class VersionResponse {
	
	/**
	 * The version of all location data
	 */
	public String locations;
	
	/**
	 * The version of Campus service data
	 */
	public String services;
	
	/**
	 * Create a version response from a JSON structure
	 * 
	 * @param root The JSON representation
	 * @return The deserialized data
	 * @throws JSONException
	 */
	public static VersionResponse deserialize(JSONObject root) throws JSONException {
		VersionResponse res = new VersionResponse();
		
		res.locations = root.getString("ServerVersion");
		res.services = root.getString("ServicesVersion");
		
		return res;
	}
}
