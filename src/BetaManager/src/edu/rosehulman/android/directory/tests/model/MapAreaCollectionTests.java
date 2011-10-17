package edu.rosehulman.android.directory.tests.model;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import edu.rosehulman.android.directory.model.LocationCollection;

public class MapAreaCollectionTests extends TestCase {
	
	public void testDeserialize() throws JSONException {
		
		JSONObject root = new JSONObject("{\"Areas\":[],\"Version\":0.1}");
		
		LocationCollection o = LocationCollection.deserialize(root);
		
		assertEquals("0.1", o.version);
		assertEquals(0, o.mapAreas.length);
	}

}
