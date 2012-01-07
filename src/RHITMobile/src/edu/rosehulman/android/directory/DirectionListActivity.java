package edu.rosehulman.android.directory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import edu.rosehulman.android.directory.model.Directions;
import edu.rosehulman.android.directory.model.Location;

public class DirectionListActivity extends Activity {
	
	public static final String EXTRA_DIRECTIONS = "DIRECTIONS";
	public static final String EXTRA_LOCATIONS = "LOCATIONS";
	
	public static Intent createIntent(Context context, Directions directions, Location[] locations) {
		Intent intent = new Intent(context, DirectionListActivity.class);
		intent.putExtra(EXTRA_DIRECTIONS, directions);
		intent.putExtra(EXTRA_LOCATIONS, locations);
		return intent;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direction_list);
    }
        

}
