package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a category of campus services hyperlinks
 */
public class TourTagsGroup {
	
	/** The name of the category */
	public String name;
	
	/** The array of entries for this category */
	public TourTag tags[];
	
	/** All categories contained under this parent */
	public TourTagsGroup children[];

	/**
	 * Creates a new, empty CampusServicesCategory
	 */
	public TourTagsGroup() {
		
	}
	
	/**
	 * Creates a new instance initialized with the given data
	 * 
	 * @param name The name of the category
	 * @param tags The array of entries
	 */
	public TourTagsGroup(String name, TourTag[] tags, TourTagsGroup[] children) {
		this.name = name;
		this.tags = tags;
		this.children = children;
	}

	private static TourTag[] deserializeTags(JSONArray array) throws JSONException {
		TourTag res[] = new TourTag[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = TourTag.deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
	
	private static TourTagsGroup[] deserializeChildren(JSONArray root) throws JSONException {
		TourTagsGroup res[] = new TourTagsGroup[root.length()];

		for (int i = 0; i < res.length; i++) {
			res[i] = TourTagsGroup.deserialize(root.getJSONObject(i));
		}
		
		return res;
	}
	
	/**
	 * Deserialize the given JSONObject into a new instance of CampusServicesCategory
	 * 
	 * @param root The JSONObject with the necessary field to create a new CampusServicesCategory
	 * @return A new CampusServicesCategory initialized from the given JSONObject
	 * @throws JSONException
	 */
	public static TourTagsGroup deserialize(JSONObject root) throws JSONException {
		TourTagsGroup res = new TourTagsGroup();
		
		res.name = root.getString("Name");
		res.tags = deserializeTags(root.getJSONArray("Tags"));
		res.children = deserializeChildren(root.getJSONArray("Children"));
		
		return res;
	}
	
}
