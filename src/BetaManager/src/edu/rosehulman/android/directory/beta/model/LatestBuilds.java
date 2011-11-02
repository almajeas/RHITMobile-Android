package edu.rosehulman.android.directory.beta.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LatestBuilds {
	
	public BuildInfo official;
	public BuildInfo rolling;
	
	public static LatestBuilds deserialize(JSONObject root) throws JSONException {
		LatestBuilds res = new LatestBuilds();
		
		Log.d("BetaManager", root.toString());
		
		if (!root.isNull("official")) {
			res.official = BuildInfo.deserialize(root.getJSONObject("official"));
		}
		
		if (!root.isNull("rolling")) {
			res.rolling = BuildInfo.deserialize(root.getJSONObject("rolling"));
		}
		
		return res;
	}

}
