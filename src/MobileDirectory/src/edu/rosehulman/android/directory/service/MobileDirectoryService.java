package edu.rosehulman.android.directory.service;

import org.json.JSONObject;

import edu.rosehulman.android.directory.model.MapAreaCollection;

/**
 * Wraps logic of communicating with the mobile directory web service into
 * native java methods.
 * 
 * Note that all function calls requiring network access are blocking and
 * should not be called on the main thread.
 */
public class MobileDirectoryService implements IMobileDirectoryService {

	private static final String HOST = "mobilewin.csse.rose-hulman.edu";
	private static final int PORT = 5600;
	
	private static ClientFactory factory;
	
	/**
	 * Set the client factory to be used when making web service calls
	 * 
	 * @param factory The client factory to use
	 */
	public static void setClientFactory(ClientFactory factory) {
		MobileDirectoryService.factory = factory;
	}
	
	/*private static void verifyThread() {
		StackTraceElement stack[] = Thread.currentThread().getStackTrace();
		for (StackTraceElement frame : stack) {
			//TODO determine proper method name/condition
			//TODO use frame.isNative() (true)
			if ("dalvik.system.NativeStart.main".equals(frame.getMethodName())) {
				throw new RuntimeException("Web access on UI thread");
			}
		}
	}*/
	
	@Override
	public MapAreaCollection getMapAreas(String currentVersion) throws Exception {
		JsonClient client = factory.makeJsonClient(HOST, PORT, "mapareas");
		if (currentVersion != null) {
			client.addParameter("version", currentVersion);
		}
		
		JSONObject root = client.execute();
		if (root == null) {
			return null;
		}
		
		return MapAreaCollection.deserialize(root);
	}

}
