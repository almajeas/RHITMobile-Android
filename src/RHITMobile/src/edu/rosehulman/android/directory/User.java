package edu.rosehulman.android.directory;

import org.json.JSONArray;
import org.json.JSONException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import edu.rosehulman.android.directory.auth.AccountAuthenticator;
import edu.rosehulman.android.directory.model.TermCode;

/**
 * Utility methods to manager the user's login state
 */
public class User {
	
	private static final String PREF_USERNAME = "Username";
	private static final String PREF_TERM_CODE = "TermCode";
	private static final String PREF_TERM_CODES = "TermCodes";

	/**
	 * Retrieve the user's username
	 * 
	 * @return The user's username
	 */
	public static String getUsername() {
		return getPrefs().getString(PREF_USERNAME, null);
	}
	
	/**
	 * Retrieve the user's password
	 * 
	 * @return The user's password, if known
	 */
	/*
	public static String getPassword() {
		SharedPreferences prefs = getPrefs();
		
		String encodedKey = prefs.getString(PREF_KEY, null);
		String encodedPassword = prefs.getString(PREF_PASSWORD, null);
		
		if (encodedKey == null || encodedPassword == null)
			return null;
		
		byte[] key = fromString(encodedKey);
		byte[] encrypted = fromString(encodedPassword);
		
		return decrypt(key, encrypted);
	}
	*/
	
	public static Account getAccount(AccountManager manager) {
		String username = getUsername();
		return findAccount(manager, username);
	}
	
	public static Account findAccount(AccountManager manager, String username) {
		Account[] accounts = manager.getAccountsByType(AccountAuthenticator.ACCOUNT_TYPE);
		
		for (Account account : accounts) {
			if (account.name.equals(username))
				return account;
		}
		return null;
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
		
		Account account = findAccount(manager, username);
		
		if (account == null) {
			//account has been deleted
			clearLogin();
		}
		
		return account != null;
	}

	/**
	 * Set the user's authentication token
	 * 
	 * @param username The user's name
	 * @param terms The available terms
	 * @param term The current term
	 * @param token The authentication token that can be used in web requests
	 */
	public static void setAccount(String username, TermCode[] terms, TermCode term) {
		JSONArray array = new JSONArray();
		for (TermCode t : terms) {
			array.put(t.code);
		}

		getPrefs().edit()
		.putString(PREF_USERNAME, username)
		.putString(PREF_TERM_CODES, array.toString())
		.putString(PREF_TERM_CODE, term.code)
		.commit();
	}

	/**
	 * Clears the user's authentication token
	 */
	public static void clearLogin() {
		getPrefs().edit()
		.remove(PREF_USERNAME)
		.remove(PREF_TERM_CODE)
		.remove(PREF_TERM_CODES)
		.commit();
	}

	/**
	 * Gets the user's current TermCode
	 * 
	 * @return The current TermCode, or the most recent one if none is set
	 */
	public static TermCode getTerm() {
		String code = getPrefs().getString(PREF_TERM_CODE, null);
		
		if (code == null)
			return null;
		
		return new TermCode(code);
	}
	
	/**
	 * Gets the available TermCodes
	 * 
	 * @return The available TermCodes, or null if none are available
	 */
	public static TermCode[] getTerms() {
		String termCodes = getPrefs().getString(PREF_TERM_CODES, null);
		if (termCodes == null)
			return null;
		
		try {
			JSONArray array = new JSONArray(termCodes);
			TermCode[] res = new TermCode[array.length()];
			for (int i = 0; i < res.length; i++)
				res[i] = new TermCode(array.getString(i));
			return res;
			
		} catch (JSONException e) {
			return null;
		} 
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
	
/*
	private static String toString(byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}
	
	private static byte[] fromString(String data) {
		return Base64.decode(data, Base64.NO_WRAP);
	}
	
	private static byte[] encrypt(byte[] key, String password) {
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
			return c.doFinal(password.getBytes());
			
		} catch (Exception e) {
			return null;
		}
	}
	
	private static String decrypt(byte[] key, byte[] password) {
		try {
			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
			return new String(c.doFinal(password));
			
		} catch (Exception e) {
			return null;
		}
	}
	
	private static byte[] generateKey() {
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			keygen.init(128);
			return keygen.generateKey().getEncoded();
			
		} catch (Exception e) {
			return null;
		}
	}
	*/
}
