package edu.rosehulman.android.directory;

import android.content.SharedPreferences;

/**
 * Utility methods to manager the user's login state
 */
public class User {
	
	private static final String PREF_USERNAME = "Username";
	private static final String PREF_COOKIE = "AuthCookie";
	
	/**
	 * Retrieve the user's authentication token
	 * 
	 * @return The authentication token that can be used in web requests
	 */
	public static String getCookie() {
		return getPrefs().getString(PREF_COOKIE, null);
	}
	
	/**
	 * Retrieve the user's username
	 * 
	 * @return The user's username
	 */
	public static String getUsername() {
		return getPrefs().getString(PREF_USERNAME, null);
	}
	
	/**
	 * Determine if the user has logged in
	 * 
	 * @return True if we have an authentication token
	 */
	public static boolean isLoggedIn() {
		return getCookie() != null;
	}

	/**
	 * Set the user's authentication token
	 * 
	 * @param token The authentication token that can be used in web requests
	 */
	public static void setCookie(String username, String token) {
		getPrefs().edit()
		.putString(PREF_USERNAME, username)
		.putString(PREF_COOKIE, token)
		.commit();
	}

	/**
	 * Clears the user's authentication token
	 */
	public static void clearLogin() {
		getPrefs().edit().remove(PREF_USERNAME).remove(PREF_COOKIE).commit();
	}
	
	private static SharedPreferences getPrefs() {
		MyApplication app = MyApplication.getInstance();
		return app.getAppPreferences();
	}
}
