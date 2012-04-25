package edu.rosehulman.android.directory;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import edu.rosehulman.android.directory.auth.AccountAuthenticator;
import edu.rosehulman.android.directory.model.TermCode;

/**
 * Utility methods to manager the user's login state
 */
public class User {
	
	private static final String PREF_USERNAME = "Username";
	private static final String PREF_TERM_CODE = "TermCode";

	/**
	 * Retrieve the user's authentication token
	 * 
	 * @return The authentication token that can be used in web requests
	 */
	public static String getCookie() {
		//TODO implement
		return null;
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
	public static boolean isLoggedIn(AccountManager manager) {
		String username = getUsername();
		
		if (username == null)
			return false;
		
		Account[] accounts = manager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
		
		for (Account account : accounts) {
			if (account.name.equals(username))
				return true;
		}
		return false;
	}

	/**
	 * Set the user's authentication token
	 * 
	 * @param username The user's name
	 * @param token The authentication token that can be used in web requests
	 */
	public static void setCookie(String username, String token) {
		
		//TODO token
		getPrefs().edit()
		.putString(PREF_USERNAME, username)
		.commit();
	}

	/**
	 * Clears the user's authentication token
	 */
	public static void clearLogin() {
		//TODO implement
		getPrefs().edit().remove(PREF_USERNAME).commit();
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
