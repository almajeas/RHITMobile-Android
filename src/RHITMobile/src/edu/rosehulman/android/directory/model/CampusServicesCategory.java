package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a category of campus services hyperlinks
 */
public class CampusServicesCategory {
	
	/** The internal id of this category (-1 if not known) */
	public long id;
	
	/** The name of the category */
	public String name;
	
	/** The array of entries for this category */
	public Hyperlink entries[];
	
	/** All categories contained under this parent */
	public CampusServicesCategory children[];

	/**
	 * Creates a new, empty CampusServicesCategory
	 */
	public CampusServicesCategory() {
		this.id = -1;
	}
	
	/**
	 * Creates a new instance initialized with the given data
	 * 
	 * @param name The name of the category
	 * @param entries The array of entries
	 */
	public CampusServicesCategory(String name, Hyperlink[] entries) {
		this.id = -1;
		this.name = name;
		this.entries = entries;
	}

	private static Hyperlink[] deserializeEntries(JSONArray array) throws JSONException {
		Hyperlink res[] = new Hyperlink[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = Hyperlink.deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
	
	private static CampusServicesCategory[] deserializeChildren(JSONArray root) throws JSONException {
		CampusServicesCategory res[] = new CampusServicesCategory[root.length()];

		for (int i = 0; i < res.length; i++) {
			res[i] = CampusServicesCategory.deserialize(root.getJSONObject(i));
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
	public static CampusServicesCategory deserialize(JSONObject root) throws JSONException {
		CampusServicesCategory res = new CampusServicesCategory();
		
		res.name = root.getString("Name");
		res.entries = deserializeEntries(root.getJSONArray("Links"));
		res.children = deserializeChildren(root.getJSONArray("Children"));
		
		return res;
	}
	
}
