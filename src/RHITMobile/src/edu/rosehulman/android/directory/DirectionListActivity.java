package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.rosehulman.android.directory.model.DirectionPath;
import edu.rosehulman.android.directory.model.Directions;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.util.ArrayUtil;

public class DirectionListActivity extends Activity {
	
	public static final String EXTRA_DIRECTIONS = "DIRECTIONS";
	public static final String EXTRA_LOCATIONS = "LOCATIONS";
	
	public static Intent createIntent(Context context, Directions directions, Location[] locations) {
		Intent intent = new Intent(context, DirectionListActivity.class);
		intent.putExtra(EXTRA_DIRECTIONS, directions);
		intent.putExtra(EXTRA_LOCATIONS, locations);
		return intent;
	}
	
	private Directions directions;
	private Location[] locations;
	
	private List<ListItem> listItems;
	
	private ListView directionList;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.direction_list);
        
        Intent intent = getIntent();
        
        directions = intent.getParcelableExtra(EXTRA_DIRECTIONS);
        
        Parcelable[] arr = intent.getParcelableArrayExtra(EXTRA_LOCATIONS);
        locations = new Location[arr.length];
        ArrayUtil.cast(arr, locations);
        
        directionList = (ListView)findViewById(R.id.directions);
        
        listItems = new ArrayList<ListItem>();
        listItems.add(new GoalListItem(locations[0].name, locations[0]));
        
        int iLoc = 1;
        for (DirectionPath path : directions.paths) {
			if (path.flag){
				listItems.add(new GoalListItem(locations[iLoc].name, locations[iLoc]));
				iLoc++;
			} else if (path.hasDirection()) {
				listItems.add(new StepListItem(path));
			}
		}
        
        directionList.setAdapter(listAdapter);
        directionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((ListItem)listAdapter.getItem(position)).click(position);
			}
		});
    }
    
    ListAdapter listAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return listItems.get(position).getView(convertView);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return listItems.get(position);
		}
		
		@Override
		public int getCount() {
			return listItems.size();
		}
	};

	private interface ListItem {
		public void click(int position);
		public View getView(View convert);
	}
	
	private class StepListItem implements ListItem {
		
		private DirectionPath path;
		
		public StepListItem(DirectionPath path) {
			this.path = path;
		}

		@Override
		public void click(int position) {
			Intent intent = CampusMapActivity.createResultIntent(position);
			setResult(RESULT_OK, intent);
			finish();
		}

		@Override
		public View getView(View convert) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.directions_list_item, null);
			
			int icon;
			switch (path.action) {
			case GO_STRAIGHT:
				icon = R.drawable.turn_straight;
				break;
			case CROSS_STREET:
				icon = R.drawable.turn_straight;
				break;
			case FOLLOW_PATH:
				icon = R.drawable.turn_straight;
				break;
			case SLIGHT_LEFT:
				icon = R.drawable.turn_slight_left;
				break;
			case SLIGHT_RIGHT:
				icon = R.drawable.turn_slight_right;
				break;
			case TURN_LEFT:
				icon = R.drawable.turn_left;
				break;
			case TURN_RIGHT:
				icon = R.drawable.turn_right;
				break;
			case SHARP_LEFT:
				icon = R.drawable.turn_sharp_left;
				break;
			case SHARP_RIGHT:
				icon = R.drawable.turn_sharp_right;
				break;
			case ENTER_BUILDING:
				icon = R.drawable.turn_enter_building;
				break;
			case EXIT_BUILDING:
				icon = R.drawable.turn_exit_building;
				break;
			case ASCEND_STAIRS:
				icon = R.drawable.turn_up_stairs;
				break;
			case DESCEND_STAIRS:
				icon = R.drawable.turn_down_stairs;
				break;
			default:
				icon = R.drawable.turn_unknown;
			}
			((ImageView)v.findViewById(R.id.icon)).setImageDrawable(getResources().getDrawable(icon));
			((TextView)v.findViewById(R.id.text)).setText(path.dir);
			((TextView)v.findViewById(R.id.distance)).setText("100 ft");
			
			return v;
		}
		
	}
	
	private class GoalListItem implements ListItem {
		
		private String name;
		private Location location;
		
		public GoalListItem(String name, Location location) {
			this.name = name;
			this.location = location;
		}

		@Override
		public void click(int position) {
			Intent intent = CampusMapActivity.createResultIntent(position);
			setResult(RESULT_OK, intent);
			finish();
		}

		@Override
		public View getView(View convert) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.directions_list_goal, null);
			
			((TextView)v.findViewById(R.id.name)).setText(name);
			
			return v;
		}
		
	}

}
