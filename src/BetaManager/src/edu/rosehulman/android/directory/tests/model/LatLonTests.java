package edu.rosehulman.android.directory.tests.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import edu.rosehulman.android.directory.model.LatLon;
import junit.framework.TestCase;

public class LatLonTests extends TestCase {
	
	public void testIntConstructor() {
		LatLon latLon = new LatLon(42, 35);
		assertEquals(42, latLon.lat);
		assertEquals(35, latLon.lon);
	}
	
	public void testDoubleConstructor() {
		LatLon latLon = new LatLon(39.4821800526708, -87.3222422754326);
		assertEquals(39482180, latLon.lat);
		assertEquals(-87322242, latLon.lon);
	}
	
	public void testGeoPoint() {
		LatLon latLon = new LatLon(39.4821800526708, -87.3222422754326);
		GeoPoint pt = latLon.asGeoPoint();
		
		assertEquals(39482180, pt.getLatitudeE6());
		assertEquals(-87322242, pt.getLongitudeE6());
	}
	
	public void testDeserializeFaulty() throws JSONException {
		
		JSONObject root = new JSONObject("{\"Lat\":39.4821800526708}");
		
		try {
			//valid JSON, but we will request an invalid field
			LatLon.deserialize(root);
		} catch (JSONException ex) {
			return;
		}
		
		assertTrue("No exception raised", false);
	}

	public void testDeserialize() throws JSONException {
		
		JSONObject root = new JSONObject("{\"Lat\":39.4821800526708,\"Long\":-87.3222422754326}");
		
		LatLon latLon = LatLon.deserialize(root);
		assertEquals(39482180, latLon.lat);
		assertEquals(-87322242, latLon.lon);
	}
	
}
