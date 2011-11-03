package edu.rosehulman.android.directory.beta;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Env {
	
	public static final String BETA_MANAGER = "edu.rosehulman.android.directory.beta";
	public static final String RHIT_MOBILE = "edu.rosehulman.android.directory";
	
	public static int getBuildNumber(Context context, String packageName) {
    	PackageInfo packageInfo;
    	try {
    		packageInfo = context.getPackageManager().getPackageInfo(packageName, 0); 
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		return packageInfo.versionCode;
	}

}
