package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a response related to user search
 */
public class UsersResponse {
	
	/** The users contained in the response */
	public ShortUser[] users;

	/**
	 * Deserialize from a json object
	 * 
	 * @param root The JSON object
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static UsersResponse deserialize(JSONObject root) throws JSONException {
		UsersResponse res = new UsersResponse();
		
		res.users = ShortUser.deserialize(root.getJSONArray("Users"));
		
		return res;
	}

}
