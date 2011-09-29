package edu.rosehulman.android.directory.tests.model;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import edu.rosehulman.android.directory.model.LatLon;
import edu.rosehulman.android.directory.model.MapArea;

public class MapAreaTests extends TestCase {
	
	public void testBasicConstructor() {
		LatLon latLon = new LatLon(39.4821800526708, -87.3222422754326);
		MapArea o = new MapArea("Test Location", latLon.lat, latLon.lon);
		
		assertEquals("Test Location", o.name);
		assertEquals(null, o.description);
		assertEquals(latLon.lat, o.center.lat);
		assertEquals(latLon.lon, o.center.lon);
		assertEquals(null, o.corners);
	}
	
	public void testDeserialize() throws JSONException {
		
		String json = "{\"Center\":" +
				"{\"Lat\":39.4837285367578,\"Long\":-87.3244470512428}," +
				"\"Corners\":[{\"Lat\":39.4837632915625,\"Long\":-87.3247013316994}]," +
				"\"Description\":\"Test Description\"," +
				"\"Id\":4," +
				"\"LabelOnHybrid\":true," +
				"\"MinZoomLevel\":15," +
				"\"Name\":\"Crapo Hall\"}";
		JSONObject root = new JSONObject(json);
		
		MapArea o = MapArea.deserialize(root);
		
		assertEquals(39483728, o.center.lat);
		assertEquals(-87324447, o.center.lon);
		
		assertEquals(1, o.corners.length);
		assertEquals(39483763, o.corners[0].lat);
		assertEquals(-87324701, o.corners[0].lon);
		assertEquals("Test Description", o.description);
		assertEquals(4, o.id);
		assertEquals(true, o.labelOnHybrid);
		assertEquals(15, o.minZoomLevel);
		assertEquals("Crapo Hall", o.name);		
	}
	
	public void testHasCorners() {
		MapArea o = new MapArea("Test", 0, 0);
		assertFalse(o.hasCorners());
		
		o.corners = new LatLon[0];
		assertTrue(o.hasCorners());
		
		o.corners = new LatLon[1];
		assertTrue(o.hasCorners());
	}

}