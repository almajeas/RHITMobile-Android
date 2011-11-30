package edu.rosehulman.android.directory.tests.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import edu.rosehulman.android.directory.CampusMapActivity;
import edu.rosehulman.android.directory.R;

public class LocationSearchTest extends ActivityInstrumentationTestCase2<CampusMapActivity> {
	
	private CampusMapActivity activity;
	
	public LocationSearchTest() {
		super("edu.rosehulman.android.directory", CampusMapActivity.class);
	}
	
	@Override
	public void setUp() {
		
		Intent intent = new Intent(Intent.ACTION_SEARCH);
		//intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(SearchManager.QUERY, "Hall");
		
		//TODO set additional flags
		setActivityIntent(intent);
		
		activity = getActivity();
	}
	
	@UiThreadTest
	public void testStartup() {
		activity.findViewById(R.id.mapview);
	}
	
	public void testSleep() {
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
