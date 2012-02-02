package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A collection of campus service categories from the server
 */
public class TourTagsResponse {

	/** Groups of tags */
	public TourTagsGroup root;
	
	/** Version associated with the given collection of objects */
	public String version;
	
	private static TourTagsGroup[] deserializeGroups(JSONArray array) throws JSONException {
		TourTagsGroup res[] = new TourTagsGroup[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = TourTagsGroup.deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
	
	/**
	 * Deserialize the given JSONObject into a new instance of TourTagsResponse
	 * 
	 * @param root The JSONObject with the necessary fields to create a new TourTagsResponse
	 * @return A new CampusServicesResponse initialized from the given JSONObject
	 * @throws JSONException
	 */
	public static TourTagsResponse deserialize(JSONObject root) throws JSONException {
		TourTagsResponse res = new TourTagsResponse();
		
		res.version = root.getString("Version");
		//FIXME implement
		//res.groups = deserializeGroups(root.getJSONArray("Categories"));
		
		return res;
	}
}
