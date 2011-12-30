package edu.rosehulman.android.directory.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Path {
	
	public String dir;
	
	public LatLon dest;
	
	public boolean flag;

	public static Path deserialize(JSONObject root) throws JSONException {
		Path res = new Path();
		
		res.dir = root.getString("Dir");
		res.dest = LatLon.deserialize(root.getJSONObject("To"));
		res.flag = root.getBoolean("Flag");
		
		return res;
	}
	
}
