package edu.rosehulman.android.directory.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an authentication response from the server
 */
public class AuthenticationResponse {
	
	private static final String DATE_PREFIX = "\\/Date(";
	private static final String DATE_SUFFIX = ")\\/";
	
	/**
	 * The user's authentication token
	 */
	public String token;
	
	public Date expiration;

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
		res.expiration = parseDate(root.getString("Expiration"));
		
		return res;
	}
	
	private static Date parseDate(String date) throws JSONException {
		if (!date.startsWith(DATE_PREFIX) || !date.endsWith(DATE_SUFFIX))
			throw new JSONException("Invalid date preffix/suffix");
		
		date = date.substring(DATE_PREFIX.length(), date.length() - DATE_SUFFIX.length()+1);
		int pos = date.indexOf('-') + date.indexOf('+') + 1;
		if (pos == -1)
			throw new JSONException("No timezone component");
		
		String ms = date.substring(0, pos);
		return new Date(Long.parseLong(ms));
	}
}
