package edu.rosehulman.android.directory.beta.model;

import org.json.JSONException;
import org.json.JSONObject;

public class BuildInfo {
	
	public int id;
	public int buildNumber;
	public String viewURL;
	public String downloadURL;
	public String publishDate;
	
	public static BuildInfo deserialize(JSONObject root) throws JSONException {
		BuildInfo res = new BuildInfo();
		
		res.id = root.getInt("id");
		res.buildNumber = root.getInt("buildNumber");
		res.viewURL = root.getString("viewURL");
		res.downloadURL = root.getString("downloadURL");
		res.publishDate = root.getString("published");
		
		return res;
	}
	
	public String getBetaManagerDownloadUrl() {
		return downloadURL.split("|")[0];
	}

	public String getMobileDownloadUrl() {
		return downloadURL.split("|")[1];
	}
	
}
