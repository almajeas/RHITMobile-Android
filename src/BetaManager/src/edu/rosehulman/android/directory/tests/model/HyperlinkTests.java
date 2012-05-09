package edu.rosehulman.android.directory.tests.model;

import junit.framework.TestCase;

import org.json.JSONObject;

import android.os.Bundle;

import edu.rosehulman.android.directory.model.Hyperlink;
import edu.rosehulman.android.directory.model.HyperlinkType;

public class HyperlinkTests extends TestCase {
	
	public void testConstructor() {
		Hyperlink link = new Hyperlink();
		
		assertNull(link.name);
		assertNull(link.url);
		
		link = new Hyperlink("Name", HyperlinkType.WEBSITE, "http://www.google.com");
		
		assertEquals("Name", link.name);
		assertEquals("http://www.google.com", link.url);
	}
	
	public void testDeserialize() throws Exception {
		String json = "{Name:\"Name\"," +
						"Url:\"http://www.google.com\"}";
		Hyperlink link = Hyperlink.deserialize(new JSONObject(json));
		
		assertEquals("Name", link.name);
		assertEquals("http://www.google.com", link.url);
	}
	
	public void testParcel() {
		Hyperlink link = new Hyperlink("Name", HyperlinkType.WEBSITE, "http://mobile.csse.rose-hulman.edu");
		
		Bundle bundle = new Bundle();
		bundle.putParcelable("key", link);
		
		link = bundle.getParcelable("key");

		assertEquals("Name", link.name);
		assertEquals("http://mobile.csse.rose-hulman.edu", link.url);
	}
	
}
