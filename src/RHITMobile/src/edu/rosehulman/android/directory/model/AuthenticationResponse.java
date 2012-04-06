package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an authentication response from the server
 */
public class AuthenticationResponse {
	
	/**
	 * The user's authentication token
	 */
	public String token;

	/**
	 * Deserialize from a json object
	 * 
	 * @param root The JSON object
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static AuthenticationResponse deserialize(JSONObject root) throws JSONException {
		AuthenticationResponse res = new AuthenticationResponse();
		
		res.token = root.getString("Token");
		
		return res;
	}
}
