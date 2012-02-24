package edu.rosehulman.android.directory;

import java.util.Arrays;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ScheduleRoomActivity extends SherlockFragmentActivity implements TabListener {
	
	public static final String EXTRA_ROOM = "ROOM";
	
	public static Intent createIntent(Context context, String room) {
		Intent intent = new Intent(context, ScheduleRoomActivity.class);
		intent.putExtra(EXTRA_ROOM, room);
		return intent;
	}
	
	private static final String[] HOURS = new String[] {"",
			"8:05am", "9:00am", "9:55am",
			"10:50am", "11:45am", "12:40pm",
			"1:35pm", "2:30pm", "3:25pm",
			"4:20pm"};

	private String room;
	
	private TaskManager taskManager = new TaskManager();
	
	private RoomScheduleWeek schedule;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_room);
		
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
		getSupportFragmentManager().beginTransaction().add(new AuthenticatedFragment(), "auth").commit();
		
		Intent intent = getIntent();
		if (!intent.hasExtra(EXTRA_ROOM)) {
			finish();
			return;
		}
		room = intent.getStringExtra(EXTRA_ROOM);
		
		LoadSchedule task = new LoadSchedule();
		taskManager.addTask(task);
		task.execute();
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			default:
				return super.onOptionsItemSelected(item); 
		}
		return true;
	}
	
	private void createTab(String tag, String label) {
		ActionBar actionBar = getSupportActionBar();
		RoomScheduleFragment frag = new RoomScheduleFragment(tag, schedule.getDay(tag));
		Tab tab = actionBar.newTab().setText(label).setTabListener(this).setTag(frag);
		actionBar.addTab(tab);
		//TODO set selected day to today
	}

	@Override
	public void onTabSelected(Tab tab) {
		RoomScheduleFragment frag = (RoomScheduleFragment)tab.getTag();
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_content, frag, frag.getDay()).commit();
	}

	@Override
	public void onTabUnselected(Tab tab) {
		RoomScheduleFragment frag = (RoomScheduleFragment)tab.getTag();
		getSupportFragmentManager().beginTransaction().remove(frag).commit();
	}

	@Override
	public void onTabReselected(Tab tab) {
	}

	private class RoomScheduleFragment extends Fragment {
		
		private String tag;
		private RoomScheduleDay day;
		
        public RoomScheduleFragment(String tag, RoomScheduleDay day) {
        	this.tag = tag;
			this.day = day;
		}

        public String getDay() {
        	return tag;
        }
        
		@Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	View root = inflater.inflate(R.layout.schedule_list, null);
			ListView list = (ListView)root.findViewById(R.id.list);
			
			list.setAdapter(new ScheduleAdapter(day.items));
			
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
					if (adapter == null)
						return;

					RoomScheduleItem item = schedule.getDay(tag).items[position];
					
					Intent intent = ScheduleCourseActivity.createIntent(ScheduleRoomActivity.this, item.course, item.section);
					startActivity(intent);
				}
				
			});
			
			return root;

        }

		private class ScheduleAdapter extends BaseAdapter {
			
			private RoomScheduleItem[] items;
			
			public ScheduleAdapter(RoomScheduleItem[] items) {
				this.items = items;
			}

			@Override
			public int getCount() {
				return items.length;
			}

			@Override
			public Object getItem(int position) {
				return items[position];
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = LayoutInflater.from(ScheduleRoomActivity.this);
				View v = convertView;
				if (v == null) {
					v = inflater.inflate(R.layout.schedule_room_list_item, null);
				}
				
				RoomScheduleItem item = items[position];
				
				TextView course = (TextView)v.findViewById(R.id.course);
				TextView time = (TextView)v.findViewById(R.id.time);
				
				course.setText(String.format("%s-%02d %s", item.course, item.section, item.courseName));
				time.setText(String.format("%s - %s", HOURS[item.hourStart], HOURS[item.hourEnd]));
				
				return v;
			}
		}

    }
	
	private class LoadSchedule extends AsyncTask<Void, Void, RoomScheduleWeek> {

		@Override
		protected RoomScheduleWeek doInBackground(Void... params) {
			
			RoomScheduleItem csse432 = 
					new RoomScheduleItem("CSSE432", "Computer Networks", 1, 5, 5);
			RoomScheduleItem csse404 = 
					new RoomScheduleItem("CSSE404", "Compiler Construction", 1, 7, 7);
			RoomScheduleItem csse304 = 
					new RoomScheduleItem("CSSE304", "Programming Language Concepts", 1, 8, 8);
			
			
			RoomScheduleItem csse404Wed = 
					new RoomScheduleItem("CSSE404", "Compiler Construction", 1, 6, 6);
			RoomScheduleItem csse499Wed = 
					new RoomScheduleItem("CSSE499", "Senior Project III", 1, 7, 9);
			
			return new RoomScheduleWeek(
					new String[] {"Mon", "Tue", "Wed", "Thu", "Fri"}, 
					new RoomScheduleDay[] {
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse432, csse404, csse304
							}),
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse432, csse404, csse304
							}),
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse404Wed, csse499Wed
							}),
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse432, csse404, csse304
							}),
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse432, csse304
							})
					});
		}
		
		@Override
		protected void onPostExecute(RoomScheduleWeek res) {
			schedule = res;
			for (String day : res.tags) {
				createTab(day, day);
			}
			
			getSupportActionBar().setSubtitle(room);
		}
		
	}
	
	private class RoomScheduleItem {
		public String course;
		public String courseName;
		public int section;
		public int hourStart;
		public int hourEnd;
		
		public RoomScheduleItem(String course, String courseName, int section, int hourStart, int hourEnd) {
			this.course = course;
			this.courseName = courseName;
			this.section = section;
			this.hourStart = hourStart;
			this.hourEnd = hourEnd;
		}
	}
	
	private class RoomScheduleDay {
		
		public RoomScheduleItem[] items;

		public RoomScheduleDay(RoomScheduleItem[] items) {
			this.items = items;
		}
	}
	
	private class RoomScheduleWeek {
		
		private String[] tags;
		private RoomScheduleDay[] days;
		
		public RoomScheduleWeek(String[] tags, RoomScheduleDay[] days) {
			this.tags = tags;
			this.days = days;
		}
		
		public RoomScheduleDay getDay(String tag) {
			return days[Arrays.asList(tags).indexOf(tag)];
		}
	}
}
