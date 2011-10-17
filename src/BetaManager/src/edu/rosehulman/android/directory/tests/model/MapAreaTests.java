package edu.rosehulman.android.directory.tests.model;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import edu.rosehulman.android.directory.model.LatLon;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.MapAreaData;

public class MapAreaTests extends TestCase {
	
	public void testBasicConstructor() {
		LatLon latLon = new LatLon(39.4821800526708, -87.3222422754326);
		Location o = new Location("Test Location", latLon.lat, latLon.lon);
		
		assertEquals("Test Location", o.name);
		assertEquals(null, o.description);
		assertEquals(latLon.lat, o.center.lat);
		assertEquals(latLon.lon, o.center.lon);
		assertEquals(null, o.mapData);
	}
	
	public void testDeserialize() throws JSONException {
		
		String json = "{\"Center\":" +
				"{\"Lat\":39.4837285367578,\"Long\":-87.3244470512428}," +
				"MapArea: {" +
				"\"Corners\":[{\"Lat\":39.4837632915625,\"Long\":-87.3247013316994}]," +
				"\"LabelOnHybrid\":true," +
				"\"MinZoomLevel\":15}," +
				"\"Description\":\"Test Description\"," +
				"\"Id\":4," +
				"\"Name\":\"Crapo Hall\"," +
				"\"Parent\":1500," +
				"\"IsPOI\":true," +
				"\"OnQuickList\":true" +
				"}";
		JSONObject root = new JSONObject(json);
		
		Location o = Location.deserialize(root);
		
		assertEquals(39483728, o.center.lat);
		assertEquals(-87324447, o.center.lon);
		
		assertEquals(1, o.mapData.corners.length);
		assertEquals(39483763, o.mapData.corners[0].lat);
		assertEquals(-87324701, o.mapData.corners[0].lon);
		assertEquals("Test Description", o.description);
		assertEquals(4, o.id);
		assertEquals(true, o.mapData.labelOnHybrid);
		assertEquals(15, o.mapData.minZoomLevel);
		assertEquals("Crapo Hall", o.name);
		assertEquals(1500, o.parentId);
		assertTrue(o.isPOI);
		assertTrue(o.isOnQuickList);
	}
	
	public void testHasCorners() {
		Location o = new Location("Test", 0, 0);
		assertNull(o.mapData);
		
		o.mapData = new MapAreaData();
		assertFalse(o.mapData.hasCorners());
		
		o.mapData.corners = new LatLon[0];
		assertTrue(o.mapData.hasCorners());
		
		o.mapData.corners = new LatLon[1];
		assertTrue(o.mapData.hasCorners());
	}

}
