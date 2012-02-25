package edu.rosehulman.android.directory;

import android.content.Intent;
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
import edu.rosehulman.android.directory.model.PersonScheduleDay;
import edu.rosehulman.android.directory.model.PersonScheduleItem;
import edu.rosehulman.android.directory.util.Ordinal;


public class PersonScheduleFragment extends Fragment {
	
	private static final String[] HOURS = new String[] {"",
			"8:05am", "9:00am", "9:55am",
			"10:50am", "11:45am", "12:40pm",
			"1:35pm", "2:30pm", "3:25pm",
			"4:20pm"};
	
	private String tag;
	private PersonScheduleDay day;
	
	public PersonScheduleFragment() {
	}
	
    public PersonScheduleFragment(String tag, PersonScheduleDay day) {
    	Bundle args = new Bundle();
    	args.putString("Day", tag);
    	args.putParcelable("Schedule", day);
    	setArguments(args);
	}

    public String getDay() {
    	return tag;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	tag = getArguments().getString("Day");
    	day = getArguments().getParcelable("Schedule");
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

				PersonScheduleItem item = day.items[position];
				
				Intent intent = ScheduleCourseActivity.createIntent(getActivity(), item.course, item.section);
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
			LayoutInflater inflater = LayoutInflater.from(getActivity());
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