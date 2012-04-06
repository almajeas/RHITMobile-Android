package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Basic information about a user
 */
public class ShortUser {

	/** The unique username of the user */
	public String username;
	
	/** The full display name of the user */
	public String fullname;
	
	/** Descriptive information about the user */
	public String subtitle;
	
	/**
	 * Deserialize from a json object
	 * 
	 * @param root The JSON object
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static ShortUser deserialize(JSONObject root) throws JSONException {
		ShortUser res = new ShortUser();
		
		res.username = root.getString("Username");
		res.fullname = root.getString("FullName");
		res.subtitle = root.getString("Subtitle");
		
		return res;
	}

	/**
	 * Deserialize from a json array
	 * 
	 * @param array The JSON array
	 * @return A new array of instances
	 * @throws JSONException On error
	 */
	public static ShortUser[] deserialize(JSONArray array) throws JSONException {
		ShortUser res[] = new ShortUser[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
}
