package edu.rosehulman.android.directory.model;

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
		res.root = TourTagsGroup.deserialize(root.getJSONObject("TagsRoot"));

		//TODO remove
		res.root.children[2].children = new TourTagsGroup[] {
				new TourTagsGroup("Clubs", new TourTag[] {
						new TourTag(400, "Fencing"),
						new TourTag(401, "Parkore"),
						new TourTag(402, "Running")
				}, new TourTagsGroup[] {})};

		return res;
	}
}
