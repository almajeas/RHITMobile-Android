package edu.rosehulman.android.directory;

import java.util.Arrays;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

public class PersonScheduleActivity extends TabActivity {
	
	public static final String EXTRA_PERSON = "PERSON";
	
	public static Intent createIntent(Context context, String person) {
		Intent intent = new Intent(context, PersonScheduleActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		return intent;
	}
	
	private static final String[] HOURS = new String[] {"",
			"8:05am", "9:00am", "9:55am",
			"10:50am", "11:45am", "12:40pm",
			"1:35pm", "2:30pm", "3:25pm",
			"4:20pm"};

	
	private TaskManager taskManager = new TaskManager();
	
	private TabHost tabHost;
	
	private PersonScheduleWeek schedule;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_person);
		
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
		
		LoadSchedule task = new LoadSchedule();
		taskManager.addTask(task);
		task.execute();
	}
	
	private void createTab(String tag, String label) {
		String header = createTabHeader(tabHost.getContext(), label);
		tabHost.addTab(tabHost.newTabSpec(tag).setIndicator(header).setContent(tabFactory));
	}
	
	private static String createTabHeader(Context context, String label) {
		return label;
	}
	
//	private static View createTabHeader(Context context, String label) {
//		View v = LayoutInflater.from(context).inflate(R.layout.schedule_tab, null);
//		TextView name = (TextView)v.findViewById(R.id.name);
//		name.setText(label);
//		return v;
//	}

	TabHost.TabContentFactory tabFactory = new TabHost.TabContentFactory() {
		
		@Override
		public View createTabContent(String tag) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View root = inflater.inflate(R.layout.schedule_list, null);
			ListView list = (ListView)root.findViewById(R.id.list);
			
			PersonScheduleDay day = schedule.getDay(tag);
			list.setAdapter(new ScheduleAdapter(day.items));
			
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
					if (adapter == null)
						return;
					
					Log.d(C.TAG, "Item clicked: " + position);
				}
				
			});
			
			return root;
		}
	};
	
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
		}
		
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
			LayoutInflater inflater = LayoutInflater.from(PersonScheduleActivity.this);
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.schedule_list_item, null);
			}
			
			PersonScheduleItem item = items[position];
			
			TextView course = (TextView)v.findViewById(R.id.course);
			TextView time = (TextView)v.findViewById(R.id.time);
			TextView room = (TextView)v.findViewById(R.id.room);
			
			course.setText(String.format("%s-%02d %s", item.course, item.section, item.courseName));
			time.setText(String.format("%s - %s", HOURS[item.hourStart], HOURS[item.hourEnd]));
			ClickableLocationSpan.linkify(room, item.room);
			
			room.setMovementMethod(LinkMovementMethod.getInstance());
			room.setFocusable(false);
			room.setFocusableInTouchMode(false);
			
			return v;
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
