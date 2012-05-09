package edu.rosehulman.android.directory.model;

/**
 * Contains information about a user and potentially their associated location
 */
public class UserInfo {
	
	/**
	 * The user's data
	 */
	public UserDataResponse data;
	
	/**
	 * The user's location, or null
	 */
	public Location location;
	
	public UserInfo(UserDataResponse data, Location location) {
		this.data = data;
		this.location = location;
	}
}
