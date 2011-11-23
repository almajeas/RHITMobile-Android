package edu.rosehulman.android.directory.beta.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Response {
	
	public boolean success;
	public String[] errors;
	
	private static String[] getErrors(JSONArray root) throws JSONException {
		String[] errors = new String[root.length()];
		Log.e("BetaManager", "Received errors:");
		for (int i = 0; i < errors.length; i++) {
			errors[i] = root.getString(i);
			Log.e("BetaManager", errors[i]);
		}
		return errors;
	}
	
	public static Response deserialize(JSONObject root) throws JSONException {
		Response res = new Response();
		
		res.success = root.getBoolean("success");
		
		if (root.has("errors")) {
			res.errors = getErrors(root.getJSONArray("errors"));
		}
		
		return res;
	}
}
