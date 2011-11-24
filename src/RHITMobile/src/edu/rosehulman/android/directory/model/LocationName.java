package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a location id/name pair
 */
public class LocationName {
	
	/** The ID of the location */
	public long id;
	
	/** The name of the location */
	public String name;
	
	/**
	 * Creates a new LocationName
	 * 
	 * @param id The id
	 * @param name The name
	 */
	public LocationName(long id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Converts a serialized LocationName to an instance
	 * 
	 * @param root The JSONObject to read from
	 * @return A new LocationName, initialized from root
	 * @throws JSONException
	 */
	public static LocationName deserialize(JSONObject root) throws JSONException {
		long id = root.getLong("Id");
		String name = root.getString("Name");
		return new LocationName(id, name);
	}

}
