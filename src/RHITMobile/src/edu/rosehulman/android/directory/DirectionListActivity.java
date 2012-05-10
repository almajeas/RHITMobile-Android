package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

import edu.rosehulman.android.directory.model.DirectionPath;
import edu.rosehulman.android.directory.model.Directions;
import edu.rosehulman.android.directory.model.Location;

public class DirectionListActivity extends SherlockActivity {
	
	public static final String EXTRA_DIRECTIONS = "DIRECTIONS";
	public static final String EXTRA_LOCATIONS = "LOCATIONS";
	
	public static Intent createIntent(Context context, Directions directions, Location[] locations) {
		Intent intent = new Intent(context, DirectionListActivity.class);
		intent.putExtra(EXTRA_DIRECTIONS, directions);
		intent.putExtra(EXTRA_LOCATIONS, locations);
		return intent;
	}
	
	private Directions directions;
	
	private List<ListItem> listItems;
	
	private ListView directionList;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.direction_list);
        
        Intent intent = getIntent();
        
        directions = intent.getParcelableExtra(EXTRA_DIRECTIONS);
        
        Parcelable[] arr = intent.getParcelableArrayExtra(EXTRA_LOCATIONS);
        Map<Long, Location> locations = new HashMap<Long, Location>();
        for (Parcelable parcelable : arr) {
        	Location location = (Location)parcelable;
        	locations.put(location.id, location);
        }
        
        directionList = (ListView)findViewById(R.id.directions);
        
        listItems = new ArrayList<ListItem>();
        
        if (directions.paths[0].location == 0) {
        	listItems.add(new NodeListItem(0, "Starting Location"));
        }

        int step = 0;
        for (int i = 0; i < directions.paths.length; i++) {
        	DirectionPath path = directions.paths[i];
        	DirectionPath next = (i < directions.paths.length-1) ? directions.paths[i+1] : null;
			if (path.flag) {
				Location location = locations.get(path.location);
				if (location == null) {
					listItems.add(new GoalListItem(step, "Unknown Location", null));
				} else {
					listItems.add(new GoalListItem(step, location.name, location));	
				}
				if (path.hasDirection()) {
					if (path.dir != null) {
						listItems.add(new StepListItem(step, path, next));
					}
					step++;
				}
			} else if (path.hasDirection()) {
				if (path.dir != null) {
					listItems.add(new StepListItem(step, path, next));
				}
				step++;
			}
		}
        //remove the last direction ("Arrive at destination")
        //listItems.remove(listItems.size()-1);
        
        directionList.setAdapter(listAdapter);
        directionList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((ListItem)listAdapter.getItem(position)).click();
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

	private abstract class ListItem {
		
		protected int step;
		
		public ListItem(int step) {
			this.step = step;
		}
		
		public void click() {
			Intent intent = CampusMapActivity.createResultIntent(step);
			setResult(RESULT_OK, intent);
			finish();
		}
		
		public abstract View getView(View convert);
	}
	
	private class StepListItem extends ListItem {
		
		private DirectionPath path;
		private DirectionPath next;
		
		public StepListItem(int step, DirectionPath path, DirectionPath next) {
			super(step);
			this.path = path;
			this.next = next;
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
			
			String distance;
			if (next == null) {
				distance = "";
			} else {
				double dist = path.distanceTo(next);
				distance = String.format("%.0f ft", dist);
			}
			
			((ImageView)v.findViewById(R.id.icon)).setImageDrawable(getResources().getDrawable(icon));
			((TextView)v.findViewById(R.id.text)).setText(path.dir);
			((TextView)v.findViewById(R.id.distance)).setText(distance);
			
			return v;
		}
		
	}
	
	private class NodeListItem extends ListItem {

		protected String name;
		
		public NodeListItem(int step, String name) {
			super(step);
			this.name = name;
		}

		@Override
		public View getView(View convert) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.directions_list_goal, null);
			
			((TextView)v.findViewById(R.id.name)).setText(name);
			
			return v;
		}
		
	}
	
	private class GoalListItem extends NodeListItem {
		
		private Location location;
		
		public GoalListItem(int step, String name, Location location) {
			super(step, name);
			this.location = location;
		}

		@Override
		public void click() {
			if (location == null)
				return;
			
			Intent intent = LocationActivity.createIntent(DirectionListActivity.this, location);
			startActivity(intent);
		}
		
	}

}
