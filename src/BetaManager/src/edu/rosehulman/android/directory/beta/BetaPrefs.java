package edu.rosehulman.android.directory.beta;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Provides access to preferences shared between BetaManager and MobileDirectory
 */
public class BetaPrefs {
	
    private static String PREFS_FILE = "PREFS_BETA";
    private static String PREF_HAS_RUN = "HAS_RUN";
    private static String PREF_USE_MOCKS = "USE_MOCKS";
    private static String PREF_ALWAYS_USE_MOCKS = "ALWAYS_USE_MOCKS";
    
    private static SharedPreferences getPrefs(Context context) {
    	return context.getSharedPreferences(PREFS_FILE, Context.MODE_WORLD_READABLE);
    }
    
    private static Editor getEditor(Context context) {
    	return getPrefs(context).edit();
    }
	
    /**
     * Set a flag indicating whether or not the user has registered for the
     * beta program
     * 
     * @param context The context to use
     * @param registered The value to set the flag to
     */
	public static void setRegistered(Context context, boolean registered) {
		Editor edit = getEditor(context);
        edit.putBoolean(PREF_HAS_RUN, registered);
        edit.commit();
	}
	
	/**
	 * Enable or disable the use of mock data sources in MobileDirectory
	 * 
	 * @param context The context to use
	 * @param useMocks True to use mocks
	 */
	public static void setUseMocks(Context context, boolean useMocks) {
		Editor edit = getEditor(context);
        edit.putBoolean(PREF_USE_MOCKS, useMocks);
        edit.commit();
	}

	/**
	 * Disable the use of mock data sources in MobileDirectory
	 * 
	 * @param context The context to use
	 */
	public static void unsetUseMocks(Context context) {
		Editor edit = getEditor(context);
        edit.remove(PREF_USE_MOCKS);
        edit.commit();
	}
	
	/**
	 * Determine if the user has elected to always use mock data sources
	 * even if the unit tests aren't being run.
	 * 
	 * @param context The context to use
	 * @return True if MobileDirectory should always use mocks; otherwise, false
	 */
	public static boolean getAlwaysUseMocks(Context context) {
		return getPrefs(context).getBoolean(PREF_ALWAYS_USE_MOCKS, false);
	}

}
