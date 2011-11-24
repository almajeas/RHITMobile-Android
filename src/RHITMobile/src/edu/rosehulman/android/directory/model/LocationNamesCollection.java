package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a versioned collection of LocationName objects
 */
public class LocationNamesCollection {
	
	/** Collection of LocationName objects */
	public LocationName locations[];
	
	/** Version associated with the given collection of LocationName objects */
	public String version;
	
	private static LocationName[] deserializeLocationNames(JSONArray array) throws JSONException {
		LocationName res[] = new LocationName[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = LocationName.deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
	
	/**
	 * Deserialize the given JSONObject into a new instance of \ref LocationNamesCollection
	 * 
	 * @param root The JSONObject with the necessary field to create a new LocationNamesCollection
	 * @return A new LocationNamesCollection initialized from the given JSONObject
	 * @throws JSONException
	 */
	public static LocationNamesCollection deserialize(JSONObject root) throws JSONException {
		LocationNamesCollection res = new LocationNamesCollection();
		
		res.version = root.getString("Version");
		res.locations = deserializeLocationNames(root.getJSONArray("Names"));
		
		return res;
	}

}
