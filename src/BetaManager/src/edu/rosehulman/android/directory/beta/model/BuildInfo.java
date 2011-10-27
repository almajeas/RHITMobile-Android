package edu.rosehulman.android.directory.beta.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class BuildInfo {
	
	public int buildNumber;
	public String viewURL;
	public String downloadURL;
	
	public static BuildInfo deserialize(JSONObject root) throws JSONException {
		Log.d("BetaManager", root.toString());
		
		BuildInfo res = new BuildInfo();
		
		res.buildNumber = root.getInt("buildNumber");
		res.viewURL = root.getString("viewURL");
		res.downloadURL = root.getString("downloadURL");
		
		return res;
	}

}
