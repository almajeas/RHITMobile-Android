package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

/**
 * Information about an individual user
 */
public class UserDataResponse {
	
	/** Basic information about the user */
	public ShortUser user;
	
	/** Basic information about the user's advisor */
	public ShortUser advisor;
	
	/** Email address */
	public String email;
	
	/** First name */
	public String firstName;

	/** Optional middle name */
	public String middleName;
	
	/** Last name */
	public String lastName;
	
	/** Current academic class (FR, SO, JR, SR) */
	public String currentClass;

	/** Current academic year (Y1, Y2, Y3, Y4) */
	public String year;
	
	/** Campus mailbox number */
	public int cm;
	
	/** Declared majors (for students) */
	public String majors;
	
	/** Associated department (for instructors) */
	public String department;
	
	/** Office/Room address */
	public String office;
	
	/** Telephone number */
	public String telephone;
	
	public String getFullName() {
		if (TextUtils.isEmpty(middleName))
			return String.format("%s %s", firstName, lastName).trim();
		else
			return String.format("%s %s %s", firstName, middleName, lastName).trim();
	}

	/**
	 * Deserialize from a json object
	 * 
	 * @param root The JSON object
	 * @return A new instance
	 * @throws JSONException On error
	 */
	public static UserDataResponse deserialize(JSONObject root) throws JSONException {
		UserDataResponse res = new UserDataResponse();
		
		res.user = ShortUser.deserialize(root.getJSONObject("User"));
		if (!root.isNull("Advisor"))
			res.advisor = ShortUser.deserialize(root.getJSONObject("Advisor"));
		res.email = root.getString("Email");
		res.firstName = root.getString("FirstName");
		res.middleName = root.optString("MiddleName");
		res.lastName = root.getString("LastName");
		res.currentClass = root.getString("Class");
		res.year = root.getString("Year");
		res.cm = root.getInt("CM");
		res.majors = root.optString("Majors");
		res.department = root.optString("Department");
		res.office = root.optString("Office");
		res.telephone = root.optString("Telephone");
		
		return res;
	}
}
