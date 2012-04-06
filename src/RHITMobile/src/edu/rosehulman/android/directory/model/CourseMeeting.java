package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Information about a single course meeting time
 */
public class CourseMeeting {

	/** The day of the meeting (M, T, W, R, F) */
	public char day;
	
	/** The time that the meeting starts (inclusive) */
	public int startPeriod;
	
	/** The time that the meeting ends (inclusive) */
	public int endPeriod;

	/** The room that the course meets in */
	public String room;
	
	/**
	 * Deserialize from a json object
	 * 
	 * @param root The JSON object
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static CourseMeeting deserialize(JSONObject root) throws JSONException {
		CourseMeeting res = new CourseMeeting();
		
		res.day = root.getString("Day").charAt(0);
		res.startPeriod = root.getInt("StartPeriod");
		res.endPeriod = root.getInt("EndPeriod");
		res.room = root.getString("Room");
		
		return res;
	}
	
	/**
	 * Deserialize from a json array
	 * 
	 * @param array The JSON array
	 * @return A new array of instances
	 * @throws JSONException On error
	 */
	public static CourseMeeting[] deserialize(JSONArray array) throws JSONException {
		CourseMeeting res[] = new CourseMeeting[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
}
