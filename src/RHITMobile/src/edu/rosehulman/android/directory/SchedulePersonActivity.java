package edu.rosehulman.android.directory;

import java.util.Arrays;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
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

import edu.rosehulman.android.directory.util.Ordinal;

public class SchedulePersonActivity extends SherlockFragmentActivity implements TabListener {
	
	public static final String EXTRA_PERSON = "PERSON";
	
	public static Intent createIntent(Context context, String person) {
		Intent intent = new Intent(context, SchedulePersonActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		return intent;
	}
	
	private static final String[] HOURS = new String[] {"",
			"8:05am", "9:00am", "9:55am",
			"10:50am", "11:45am", "12:40pm",
			"1:35pm", "2:30pm", "3:25pm",
			"4:20pm"};

	private String person;
	
	private TaskManager taskManager = new TaskManager();
	
	private PersonScheduleWeek schedule;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_person);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
		getSupportFragmentManager().beginTransaction().add(new AuthenticatedFragment(), "auth").commit();
        
		Intent intent = getIntent();
		if (!intent.hasExtra(EXTRA_PERSON)) {
			finish();
			return;
		}
		person = intent.getStringExtra(EXTRA_PERSON);
		
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
		PersonScheduleFragment frag = new PersonScheduleFragment(tag, schedule.getDay(tag));
		Tab tab = actionBar.newTab().setText(label).setTabListener(this).setTag(frag);
		actionBar.addTab(tab);
		//TODO set selected day to today
	}

	@Override
	public void onTabSelected(Tab tab) {
		PersonScheduleFragment frag = (PersonScheduleFragment)tab.getTag();
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_content, frag, frag.getDay()).commit();
	}

	@Override
	public void onTabUnselected(Tab tab) {
		PersonScheduleFragment frag = (PersonScheduleFragment)tab.getTag();
		getSupportFragmentManager().beginTransaction().remove(frag).commit();
	}

	@Override
	public void onTabReselected(Tab tab) {
	}
	
	private class PersonScheduleFragment extends Fragment {
		
		private String tag;
		private PersonScheduleDay day;
		
        public PersonScheduleFragment(String tag, PersonScheduleDay day) {
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

					PersonScheduleItem item = schedule.getDay(tag).items[position];
					
					Intent intent = ScheduleCourseActivity.createIntent(SchedulePersonActivity.this, item.course, item.section);
					startActivity(intent);
				}
				
			});
			
			return root;

        }
        
        private class ScheduleAdapter extends BaseAdapter {
    		
    		private PersonScheduleItem[] items;
    		
    		public ScheduleAdapter(PersonScheduleItem[] items) {
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
    			LayoutInflater inflater = LayoutInflater.from(SchedulePersonActivity.this);
    			View v = convertView;
    			if (v == null) {
    				v = inflater.inflate(R.layout.schedule_person_list_item, null);
    			}
    			
    			PersonScheduleItem item = items[position];
    			
    			TextView course = (TextView)v.findViewById(R.id.course);
    			TextView time = (TextView)v.findViewById(R.id.time);
    			TextView room = (TextView)v.findViewById(R.id.room);
    			
    			String hour;
    			if (item.hourStart == item.hourEnd) {
    				hour = String.format("%s - %s (%s hour)", 
    						HOURS[item.hourStart], HOURS[item.hourEnd+1], 
    						Ordinal.convert(item.hourStart));
    			} else {
    				hour = String.format("%s - %s (%s - %s hour)", 
    						HOURS[item.hourStart], HOURS[item.hourEnd+1], 
    						Ordinal.convert(item.hourStart),
    						Ordinal.convert(item.hourEnd+1));
    			}
    			
    			course.setText(String.format("%s-%02d %s", item.course, item.section, item.courseName));
    			
    			time.setText(hour);
    			
    			ClickableLocationSpan.linkify(room, item.room);
    			
    			room.setMovementMethod(LinkMovementMethod.getInstance());
    			room.setFocusable(false);
    			room.setFocusableInTouchMode(false);
    			
    			return v;
    		}
    	}

    }
	
	private class LoadSchedule extends AsyncTask<Void, Void, PersonScheduleWeek> {

		@Override
		protected PersonScheduleWeek doInBackground(Void... params) {
			
			PersonScheduleItem csse432 = 
					new PersonScheduleItem("CSSE432", "Computer Networks", 1, 5, 5, "O205");
			PersonScheduleItem csse404 = 
					new PersonScheduleItem("CSSE404", "Compiler Construction", 1, 7, 7, "O267");
			PersonScheduleItem csse304 = 
					new PersonScheduleItem("CSSE304", "Programming Language Concepts", 1, 8, 8, "O257");
			
			
			PersonScheduleItem csse404Wed = 
					new PersonScheduleItem("CSSE404", "Compiler Construction", 1, 6, 6, "O267");
			PersonScheduleItem csse499Wed = 
					new PersonScheduleItem("CSSE499", "Senior Project III", 1, 7, 9, "O201");
			
			return new PersonScheduleWeek(
					new String[] {"Mon", "Tue", "Wed", "Thu", "Fri"}, 
					new PersonScheduleDay[] {
							new PersonScheduleDay(new PersonScheduleItem[] {
									csse432, csse404, csse304
							}),
							new PersonScheduleDay(new PersonScheduleItem[] {
									csse432, csse404, csse304
							}),
							new PersonScheduleDay(new PersonScheduleItem[] {
									csse404Wed, csse499Wed
							}),
							new PersonScheduleDay(new PersonScheduleItem[] {
									csse432, csse404, csse304
							}),
							new PersonScheduleDay(new PersonScheduleItem[] {
									csse432, csse304
							})
					});
		}
		
		@Override
		protected void onPostExecute(PersonScheduleWeek res) {
			schedule = res;
			for (String day : res.tags) {
				createTab(day, day);
			}
			getSupportActionBar().setSubtitle(person);
		}
		
	}
	
	
	
	private class PersonScheduleItem {
		public String course;
		public String courseName;
		public int section;
		public int hourStart;
		public int hourEnd;
		public String room;
		
		public PersonScheduleItem(String course, String courseName, int section, int hourStart, int hourEnd, String room) {
			this.course = course;
			this.courseName = courseName;
			this.section = section;
			this.hourStart = hourStart;
			this.hourEnd = hourEnd;
			this.room = room;
		}
	}
	
	private class PersonScheduleDay {
		
		public PersonScheduleItem[] items;

		public PersonScheduleDay(PersonScheduleItem[] items) {
			this.items = items;
		}
	}
	
	private class PersonScheduleWeek {
		
		private String[] tags;
		private PersonScheduleDay[] days;
		
		public PersonScheduleWeek(String[] tags, PersonScheduleDay[] days) {
			this.tags = tags;
			this.days = days;
		}
		
		public PersonScheduleDay getDay(String tag) {
			return days[Arrays.asList(tags).indexOf(tag)];
		}
	}

}
