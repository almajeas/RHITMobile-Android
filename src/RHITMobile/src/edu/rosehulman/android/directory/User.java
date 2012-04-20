package edu.rosehulman.android.directory;

import edu.rosehulman.android.directory.model.TermCode;
import android.content.SharedPreferences;

/**
 * Utility methods to manager the user's login state
 */
public class User {
	
	private static final String PREF_USERNAME = "Username";
	private static final String PREF_COOKIE = "AuthCookie";
	private static final String PREF_TERM_CODE = "TermCode";
	
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
	 * @param username The user's name
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

	/**
	 * Gets the user's current TermCode
	 * 
	 * @return The current TermCode, or the most recent one if none is set
	 */
	public static TermCode getTerm() {
		String code = getPrefs().getString(PREF_TERM_CODE, null);
		
		if (code == null) {
			//FIXME remove
			return TermCodes.generateTerms()[0];
		}
		
		return new TermCode(code);
	}
	
	/**
	 * Set the user's preferred term code
	 * 
	 * @param term The new term code to use
	 */
	public static void setTerm(TermCode term) {
		if (getTerm().equals(term))
			return;
		
		getPrefs().edit()
		.putString(PREF_TERM_CODE, term.code)
		.commit();
	}
	
	private static SharedPreferences getPrefs() {
		MyApplication app = MyApplication.getInstance();
		return app.getAppPreferences();
	}
}
