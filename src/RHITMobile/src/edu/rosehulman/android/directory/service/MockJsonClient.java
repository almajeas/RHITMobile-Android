package edu.rosehulman.android.directory.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import edu.rosehulman.android.directory.C;

/**
 * Like the real JsonClient, except that it doesn't actually
 * talk to a web server.  It simply serves static data and will
 * be useful for unit testing in a controlled environment.
 */
public class MockJsonClient extends JsonClient {
	
	private static final String LOCATIONS_VERSION = "0.134";
	
	private static Context context;
	
	public static void setContext(Context context) {
		MockJsonClient.context = context;
	}
	
	/**
	 * Create a new MockJsonClient
	 * 
	 * @param host The host to send the request to (i.e. example.com)
	 * @param port The port the remote service is running on
	 * @param path The request path (i.e. index.htm)
	 */
	public MockJsonClient(String host, int port, String path) {
		super(host, port, path);
	}
	
	private String readFile(String path) throws Exception {
		path = "response_" + path.replace('/', '_');
		Log.d(C.TAG, "Reading resource: " + path);
		int res = context.getResources().getIdentifier(path, "raw", context.getPackageName());
		InputStream stream = context.getResources().openRawResource(res);
		InputStreamReader fin = new InputStreamReader(stream, "UTF8");
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[2048];
		int read;
		while ((read = fin.read(buffer)) != -1) {
			builder.append(buffer, 0, read);
		}
		fin.close();
		return builder.toString();
	}
	
	private JSONObject readJsonFile(String path) throws Exception {
		return new JSONObject(readFile(path));
	}
	
	private JSONObject getResponse() {
		Map<String, String> params = new HashMap<String, String>();
		for (NameValuePair pair : queryParams) {
			params.put(pair.getName(), pair.getValue());
		}
		
		String version = params.get("version");
		
		Log.d(C.TAG, "Handling request: " + path);
		
		
		try {
			if ("locations/data/top".equals(path)) {
				
				if (LOCATIONS_VERSION.equals(version)) {
					//data up to date
					return null;
				}
				
				return readJsonFile(path);
				
			} else if (path.startsWith("locations/data/within/")) {
				return readJsonFile(path);
				
			} else if ("locations/names".equals(path)) {
				return readJsonFile(path + "__s_" + params.get("s").toLowerCase());
				
			} else {
				return readJsonFile(path);
			}
		} catch (Exception ex) {
			//we messed up
			throw new RuntimeException("Failed to parse JSON string", ex);
		}		
	}
	
	@Override
	public JSONObject execute() {
		JSONObject response = getResponse();
		
		try {
			//simulate some network latency
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//nom
		}
		
		return response;
	}

}
