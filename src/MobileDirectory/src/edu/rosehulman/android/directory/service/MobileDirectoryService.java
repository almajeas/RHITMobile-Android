package edu.rosehulman.android.directory.service;

import org.json.JSONObject;

import edu.rosehulman.android.directory.model.MapAreaCollection;

public class MobileDirectoryService implements IMobileDirectoryService {

	private static final String HOST = "mobilewin.csse.rose-hulman.edu";
	private static final int PORT = 5600;
	
	@Override
	public MapAreaCollection getMapAreas(String currentVersion) throws Exception {
		JsonClient client = new JsonClient(HOST, PORT, "mapareas");
		if (currentVersion != null) {
			client.addParameter("version", currentVersion);
		}
		JSONObject root = client.execute();
		
		return MapAreaCollection.deserialize(root);
	}

}
