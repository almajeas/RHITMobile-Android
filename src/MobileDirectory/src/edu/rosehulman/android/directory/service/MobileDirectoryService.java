package edu.rosehulman.android.directory.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rosehulman.android.directory.model.LatLon;
import edu.rosehulman.android.directory.model.MapArea;
import edu.rosehulman.android.directory.model.MapAreaCollection;

public class MobileDirectoryService implements IMobileDirectoryService {

	@Override
	public MapAreaCollection getMapAreas(String currentVersion) throws Exception {
		HttpClient http = new DefaultHttpClient();
		String url = "http://mobilewin.csse.rose-hulman.edu:5600/mapareas";
		if (currentVersion != null) {
			url += "?version=" + currentVersion;
		}
    	HttpGet request = new HttpGet(url);
    	
		ResponseHandler<String> handler = new BasicResponseHandler();
		HttpResponse response = http.execute(request);
		if (response.getStatusLine().getStatusCode() == 204) {
			return null;
		}
		
		String responseBody = handler.handleResponse(response);
		JSONObject root = new JSONObject(responseBody);
		
		return getCollection(root);
	}
	
	public MapAreaCollection getCollection(JSONObject root) throws JSONException {
		MapAreaCollection collection = new MapAreaCollection();
		collection.version = root.getString("Version");
		
		JSONArray areas = root.getJSONArray("Areas");
		collection.mapAreas = new MapArea[areas.length()];
		for (int i = 0; i < collection.mapAreas.length; i++) {
			collection.mapAreas[i] = getMapArea(areas.getJSONObject(i));
		}
		
		return collection;
	}
	
	public MapArea getMapArea(JSONObject root) throws JSONException {
		MapArea mapArea = new MapArea();
		
		mapArea.id = root.getInt("Id");
		mapArea.name = root.getString("Name");
		mapArea.description = root.getString("Description");
		mapArea.labelOnHybrid = root.getBoolean("LabelOnHybrid");
		mapArea.minZoomLevel = root.getInt("MinZoomLevel");
		mapArea.corners = getCorners(root.getJSONArray("Corners"));
		mapArea.center = LatLon.deserialize(root.getJSONObject("Center"));;
		
		return mapArea;
	}
	
	public LatLon[] getCorners(JSONArray array) throws JSONException {
		LatLon[] res = new LatLon[array.length()];
		for (int i = 0; i < res.length; i++) {
			res[i] = LatLon.deserialize(array.getJSONObject(i));
		}
		return res;
	}

}
