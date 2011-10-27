package edu.rosehulman.android.directory.beta.service;

import org.json.JSONObject;

import edu.rosehulman.android.directory.beta.model.LatestBuilds;

public class BetaService {
	
	private static final String HOST = "mobile.csse.rose-hulman.edu";
	private static final int PORT = 80;
	private static final String PATH = "beta/actions.cgi";
	
	public BetaService() {
	}

	public String register(String email, String deviceIdentifier, String osInfo, 
			String model, String buildNumber, 
			String name, String carrier) throws Exception {
		JsonClient client = new JsonClient(HOST, PORT, PATH);
		
		//set static parameters
		client.addParameter("action", "register");
		client.addParameter("platform", "android");
		
		//set required parameters
		client.addParameter("email", email);
		client.addParameter("deviceIdentifier", deviceIdentifier);

		client.addParameter("OSInfo", osInfo);
		client.addParameter("model", model);
		client.addParameter("buildNumber", buildNumber);
		
		//add optional parameters, if they exist
		if (!"".equals(name))
			client.addParameter("name", name);
		
		if (!"".equals(carrier))
			client.addParameter("carrier", carrier);

		JSONObject root = client.execute();
		
		boolean success = root.getBoolean("success");
		if (!success) {
			return null;
		}
		//root.getBoolean("newUser");
		//root.getBoolean("newDevice");
        return root.getString("authToken");
	}
	
	public LatestBuilds getLatestBuilds() throws Exception {
		JsonClient client = new JsonClient(HOST, PORT, PATH);

		client.addParameter("action", "getLatestBuilds");
		client.addParameter("platform", "android");
		
		JSONObject root = client.execute();
		
		boolean success = root.getBoolean("success");
		if (!success) {
			return null;
		}
		
		return LatestBuilds.deserialize(root);
	}

}
