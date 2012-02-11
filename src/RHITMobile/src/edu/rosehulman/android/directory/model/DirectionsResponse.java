package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a response from the server when requesting directions/tours
 */
public class DirectionsResponse {

	/** percent done (0-100) */
	public int done;
	
	/** The ID of the request */
	public int requestID;
	
	/** The result, or null if done != 100 */
	public Directions result;
	
	/**
	 * Deserialize an instance from a JSON object
	 * 
	 * @param root The JSONObject
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static DirectionsResponse deserialize(JSONObject root) throws JSONException {
		DirectionsResponse res = new DirectionsResponse();
		
		res.done = root.getInt("Done");
		res.requestID = root.getInt("RequestId");
		
		if (root.isNull("Result"))
			res.result = null;
		else
			res.result = Directions.deserialize(root.getJSONObject("Result"));
		
		return res;
	}
	
}
