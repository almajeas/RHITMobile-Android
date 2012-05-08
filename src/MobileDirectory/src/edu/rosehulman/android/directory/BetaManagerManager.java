package edu.rosehulman.android.directory;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**
 * Provides access to shared preferences between BetaManager and MobileDirectory
 */
public class BetaManagerManager extends ContextWrapper {

	/** Shown when the application starts */
	public static String ACTION_SHOW_STARTUP = "edu.rosehulman.android.directory.beta.SHOW_STARTUP";
	/** Shown when the user is not registered */
    public static String ACTION_SHOW_REGISTER = "edu.rosehulman.android.directory.beta.SHOW_REGISTER";
    /** Shown when the user starts the beta manager */
    public static String ACTION_SHOW_BETA_MANAGER = "edu.rosehulman.android.directory.beta.SHOW_BETA_MANAGER";
    
    public static String PREFS_FILE = "PREFS_BETA";
    public static String PREF_BETA_ENABLED = "BETA_ENABLED";
    public static String PREF_HAS_RUN = "HAS_RUN";
    public static String PREF_USE_MOCKS = "USE_MOCKS";
    public static String PREF_ALWAYS_USE_MOCKS = "ALWAYS_USE_MOCKS";
	
	private static final String BETA_PACKAGE = "edu.rosehulman.android.directory.beta";
    private static final String BETA_ACTIVITY = "edu.rosehulman.android.directory.beta.BetaManagerActivity";
	
    /**
     * Create a new BetaManagerManager
     * @param base The context to use
     */
	public BetaManagerManager(Context base) {
		super(base);
	}

	/**
	 * Determine if the BetaManager package is installed on the system
	 * 
	 * @return True if the BetaManager package is installed
	 */
    public boolean hasBetaManager() {
    	PackageManager packageManager = getPackageManager(); 
    	ComponentName name = new ComponentName(BETA_PACKAGE, BETA_ACTIVITY);
    	try {
    		packageManager.getActivityInfo(name, PackageManager.GET_META_DATA);
    	} catch (NameNotFoundException ex) {
    		return false;
    	}

    	return true;
    }
    
    private SharedPreferences getBetaPreferences() throws NameNotFoundException {
    	return createPackageContext(BETA_PACKAGE, 0).getSharedPreferences(PREFS_FILE, MODE_WORLD_READABLE);	
    }

    /**
     * Determine if the beta system is enabled
     * 
     * @return True if enabled; false otherwise
     */
    public boolean isBetaEnabled() {
    	try {
    		return getBetaPreferences().getBoolean(PREF_BETA_ENABLED, false);	
    	} catch (NameNotFoundException ex) {
    		Log.e(C.TAG, "Failed to open beta shared preferences");
    		return false;
    	}    	
    }
    
    /**
     * Determine if the user is registered for the beta system
     * 
     * @return True if the user is registered
     */
    public boolean isBetaRegistered() {
    	try {
    		return getBetaPreferences().getBoolean(PREF_HAS_RUN, false);	
    	} catch (NameNotFoundException ex) {
    		Log.e(C.TAG, "Failed to open beta shared preferences");
    		return false;
    	}    	
    }
    
    /**
     * Determine if MobileDirectory should use mock data sources
     * 
     * @return True if MobileDirectory should use mock data sources
     */
    public boolean isMocking() {
    	try {
    		SharedPreferences prefs = getBetaPreferences();
    		if (prefs.contains(PREF_USE_MOCKS)) {
    			return prefs.getBoolean(PREF_USE_MOCKS, false);
    		}
    		return prefs.getBoolean(PREF_ALWAYS_USE_MOCKS, false);	
    	} catch (NameNotFoundException ex) {
    		Log.d(C.TAG, "Failed to open beta shared preferences");
    		return false;
    	}
    }
    
    /**
     * Spawn a BetaManager activity
     * 
     * @param action The activity to start
     */
    public void launchBetaActivity(String action) {
    	Intent intent = new Intent(action);
    	try {
    		startActivity(intent);
    	} catch (ActivityNotFoundException ex) {
    		Log.e(C.TAG, "Activity not found", ex);
    	}
    }	
}
