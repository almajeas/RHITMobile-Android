package edu.rosehulman.android.directory.tests.ui;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import com.google.android.maps.MapView;

import edu.rosehulman.android.directory.CampusMapActivity;
import edu.rosehulman.android.directory.R;

public class MainActivityTest extends
		ActivityInstrumentationTestCase2<CampusMapActivity> {

	public MainActivityTest() {
		super("edu.rosehulman.android.directory", CampusMapActivity.class);
	}

	private CampusMapActivity mActivity;
	// private Instrumentation mInstrumentation;

	private MapView m_mapView;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		mActivity = getActivity();

		m_mapView = (MapView) mActivity.findViewById(R.id.mapview);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@UiThreadTest
	public void testMapViewState() {
		assertTrue(m_mapView.isSatellite());
		assertFalse(m_mapView.isStreetView());
		assertTrue(m_mapView.getOverlays().size() > 0);
	}

}
