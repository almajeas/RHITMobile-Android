package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

public class DirectionsResponse {

	public int done;
	
	public int requestID;
	
	public Directions result;
	
	public static DirectionsResponse deserialize(JSONObject root) throws JSONException {
		DirectionsResponse res = new DirectionsResponse();
		
		res.done = root.getInt("Done");
		res.requestID = root.getInt("RequestId");
		res.result = Directions.deserialize(root.getJSONObject("Result"));
		
		return res;
	}
	
}
