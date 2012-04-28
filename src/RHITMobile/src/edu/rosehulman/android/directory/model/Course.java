package edu.rosehulman.android.directory.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Information about a course
 */
public class Course {
	
	/** The associated term */
	public int term;
	
	/** The course's CRN number */
	public int crn;
	
	/** The course code (ex CSSE120) */
	public String course;
	
	/** Course name */
	public String title;
	
	/** Number of credits */
	public int credits;
	
	/** Instructor */
	public ShortUser instructor;
	
	/** Current enrollment */
	public int enrolled;
	
	/** Maximum enrollment */
	public int maxEnrollment;
	
	/** Day of final exam */
	public char finalDay;
	
	/** Hour of final exam */
	public int finalHour;
	
	/** Room for final exam */
	public String finalRoom;
	
	/** Miscellaneous comments */
	public String comments;
	
	/** Schedule for course */
	public CourseMeeting[] schedule;

	/** Course roster */
	public ShortUser[] students;

	/**
	 * Deserialize from a json object
	 * 
	 * @param root The JSON object
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static Course deserialize(JSONObject root) throws JSONException {
		Course res = new Course();
		
		res.term = root.getInt("Term");
		res.crn = root.getInt("CRN");
		res.course = root.getString("CourseNumber");
		res.title = root.getString("Title");
		res.credits = root.getInt("Credits");
		res.instructor = ShortUser.deserialize(root.getJSONObject("Instructor"));
		res.enrolled = root.getInt("Enrolled");
		res.maxEnrollment = root.getInt("MaxEnrolled");
		res.finalDay = root.getString("FinalDay").charAt(0);
		res.finalHour = root.getInt("FinalHour");
		res.finalRoom = root.getString("FinalRoom");
		res.comments = root.optString("Comments");
		
		if (!root.isNull("Schedule")) {
			res.schedule = CourseMeeting.deserialize(root.getJSONArray("Schedule"));
		}
		
		if (!root.isNull("Students")) {
			res.students = ShortUser.deserialize(root.getJSONArray("Students"));
		}
		
		return res;
	}
	
	/**
	 * Deserialize from a json array
	 * 
	 * @param array The JSON array
	 * @return A new array of instances
	 * @throws JSONException On error
	 */
	public static Course[] deserialize(JSONArray array) throws JSONException {
		Course res[] = new Course[array.length()];
		
		for (int i = 0; i < res.length; i++) {
			res[i] = deserialize(array.getJSONObject(i));
		}
		
		return res;
	}
}
