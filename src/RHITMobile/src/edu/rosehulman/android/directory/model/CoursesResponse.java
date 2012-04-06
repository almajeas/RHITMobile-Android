package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a response related to course information
 */
public class CoursesResponse {
	
	/** The courses contained in the response */
	public Course[] courses;
	

	/**
	 * Deserialize from a json object
	 * 
	 * @param root The JSON object
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static CoursesResponse deserialize(JSONObject root) throws JSONException {
		CoursesResponse res = new CoursesResponse();
		
		res.courses = Course.deserialize(root.getJSONArray("Courses"));
		
		return res;
	}

}
