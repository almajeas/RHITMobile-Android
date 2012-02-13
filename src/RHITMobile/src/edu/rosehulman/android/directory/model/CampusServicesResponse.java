package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A collection of campus service categories from the server
 */
public class CampusServicesResponse {

	/** Categories of Campus Services */
	public CampusServicesCategory root;
	
	/** Version associated with the given collection of objects */
	public String version;
	
	/**
	 * Deserialize the given JSONObject into a new instance of CampusServicesResponse
	 * 
	 * @param root The JSONObject with the necessary field to create a new CampusServicesResponse
	 * @return A new CampusServicesResponse initialized from the given JSONObject
	 * @throws JSONException
	 */
	public static CampusServicesResponse deserialize(JSONObject root) throws JSONException {
		CampusServicesResponse res = new CampusServicesResponse();
		
		res.version = root.getString("Version");
		res.root = CampusServicesCategory.deserialize(root.getJSONObject("ServicesRoot"));
				
		return res;
	}
}
