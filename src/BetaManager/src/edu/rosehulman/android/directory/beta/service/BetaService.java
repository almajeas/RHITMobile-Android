package edu.rosehulman.android.directory.beta.service;

import org.json.JSONObject;

import edu.rosehulman.android.directory.beta.model.LatestBuilds;

public class BetaService {
	
	private static final String HOST = "rhitmobilebeta.heroku.com";
	//private static final String HOST = "rhitmobilebeta-test.heroku.com";
	
	private static final int PORT = 80;
	
	public BetaService() {
	}

	public String register(String email, String deviceIdentifier, String buildNumber, 
			String osInfo, String model, String name, String carrier, String info) throws Exception {
		JsonClient client = new JsonClient(HOST, PORT, "device/register");
		client.setMethod(HttpMethod.POST);
		
		//set static parameters
		client.addPostParameter("platform", "android");
		
		//set required parameters
		client.addPostParameter("email", email);
		client.addPostParameter("deviceID", deviceIdentifier);

		client.addPostParameter("build", buildNumber);
		
		//add optional parameters, if they exist
		
		if (name != null && !"".equals(name))
			client.addPostParameter("name", name);
		
		if (carrier != null && !"".equals(carrier))
			client.addPostParameter("carrier", carrier);
		
		if (osInfo != null && !"".equals(osInfo))
			client.addPostParameter("operatingSystem", osInfo);
		
		if (model != null && !"".equals(model))
			client.addPostParameter("model", model);
		
		if (info != null && !"".equals(info))
			client.addPostParameter("additionalInfo", info);

		JSONObject root = client.execute();
		
		return root.getString("authToken");
	}
	
	public LatestBuilds getLatestBuilds() throws Exception {
		JsonClient client = new JsonClient(HOST, PORT, "platform/android/builds/current");

		JSONObject root = client.execute();
		
		return LatestBuilds.deserialize(root);
	}

}
