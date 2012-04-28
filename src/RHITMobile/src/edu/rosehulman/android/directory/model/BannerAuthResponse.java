package edu.rosehulman.android.directory.model;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents an authentication response from the server
 */
public class BannerAuthResponse {
	
	private static final String DATE_PREFIX = "/Date(";
	private static final String DATE_SUFFIX = ")/";
	
	/**
	 * The user's authentication token
	 */
	public String token;
	
	public Date expiration;
	
	public TermCode[] terms;
	
	public TermCode currentTerm;

	/**
	 * Deserialize from a json object
	 * 
	 * @param root The JSON object
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static BannerAuthResponse deserialize(JSONObject root) throws JSONException {
		BannerAuthResponse res = new BannerAuthResponse();

		res.token = root.getString("Token");
		res.expiration = parseDate(root.getString("Expiration"));
		
		res.terms = TermCode.deserialize(root.getJSONArray("Terms"));
		res.currentTerm = new TermCode(root.getString("CurrentTerm"));
		
		return res;
	}
	
	private static Date parseDate(String date) throws JSONException {
		if (!date.startsWith(DATE_PREFIX) || !date.endsWith(DATE_SUFFIX))
			throw new JSONException("Invalid date preffix/suffix");
		
		date = date.substring(DATE_PREFIX.length(), date.length() - DATE_SUFFIX.length());
		int pos = date.indexOf('-') + date.indexOf('+') + 1;
		
		String ms;
		if (pos == -1) {
			//no timezone
			ms = date;
		} else {
			//remove timezone
			ms = date.substring(0, pos);
		}

		return new Date(Long.parseLong(ms));
	}
}
