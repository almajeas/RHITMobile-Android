package edu.rosehulman.android.directory.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

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
		
		return MapAreaCollection.deserialize(root);
	}

}
