package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

public class DirectionsResponse {

	public boolean done;
	
	public int requestID;
	
	public Directions result;
	
	public static DirectionsResponse deserialize(JSONObject root) throws JSONException {
		DirectionsResponse res = new DirectionsResponse();
		
		res.done = root.getBoolean("Done");
		res.requestID = root.getInt("RequestID");
		res.result = Directions.deserialize(root.getJSONObject("Result"));
		
		return res;
	}
	
}
