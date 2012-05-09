package edu.rosehulman.android.directory.fragments;

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
import edu.rosehulman.android.directory.R;
import edu.rosehulman.android.directory.ScheduleCourseActivity;
import edu.rosehulman.android.directory.model.RoomScheduleDay;
import edu.rosehulman.android.directory.model.RoomScheduleItem;
import edu.rosehulman.android.directory.model.TermCode;
import edu.rosehulman.android.directory.util.Ordinal;
import edu.rosehulman.android.directory.util.TimeUtil;

public class ScheduleRoomFragment extends Fragment {

	private TermCode term;
	private String tag;
	private RoomScheduleDay day;
	
	public static Bundle buildArguments(TermCode term, String tag, RoomScheduleDay day) {
		Bundle args = new Bundle();
		args.putParcelable("Term", term);
    	args.putString("Day", tag);
    	args.putParcelable("Schedule", day);
    	return args;
	}
	
	public ScheduleRoomFragment() {
	}
	
    public ScheduleRoomFragment(TermCode term, String tag, RoomScheduleDay day) {
    	setArguments(buildArguments(term, tag, day));
	}

    public String getDay() {
    	return tag;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	term = getArguments().getParcelable("Term");
    	tag = getArguments().getString("Day");
    	day = getArguments().getParcelable("Schedule");
    	
    }
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View root = inflater.inflate(R.layout.schedule_list, null);
		ListView list = (ListView)root.findViewById(R.id.list);
		
		list.setAdapter(new ScheduleAdapter(day));
		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
				if (adapter == null)
					return;

				RoomScheduleItem item = day.getItem(position);
				
				Intent intent = ScheduleCourseActivity.createIntent(getActivity(), term, item.crn, item.course);
				startActivity(intent);
			}
			
		});
		
		return root;

    }
    
    private class ScheduleAdapter extends BaseAdapter {
		
    	private String[] hours_start;
    	private String[] hours_end;
    	
		private RoomScheduleDay day;
		
		public ScheduleAdapter(RoomScheduleDay day) {
			this.day = day;

	    	hours_start = getResources().getStringArray(R.array.hours_start);
	    	hours_end = getResources().getStringArray(R.array.hours_end);
		}

		@Override
		public int getCount() {
			return day.count();
		}

		@Override
		public Object getItem(int position) {
			return day.getItem(position);
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
			
			RoomScheduleItem item = day.getItem(position);
			
			TextView course = (TextView)v.findViewById(R.id.course);
			TextView time = (TextView)v.findViewById(R.id.time);
			TextView room = (TextView)v.findViewById(R.id.room);
			
			String hour;
			if (item.hourStart > 100 || item.hourEnd > 100) {
				//time
				hour = String.format("%s - %s",
						TimeUtil.formatTime(item.hourStart),
						TimeUtil.formatTime(item.hourEnd));
			} else {
				//hour
				if (item.hourStart == item.hourEnd) {
					hour = String.format("%s - %s (%s hour)", 
							hours_start[item.hourStart], hours_end[item.hourEnd], 
							Ordinal.convert(item.hourStart));
				} else {
					hour = String.format("%s - %s (%s - %s hour)", 
							hours_start[item.hourStart], hours_end[item.hourEnd], 
							Ordinal.convert(item.hourStart),
							Ordinal.convert(item.hourEnd));
				}
			}
			
			course.setText(String.format("%s %s", item.course, item.courseName));
			
			time.setText(hour);
			
			room.setVisibility(View.GONE);
			
			room.setMovementMethod(LinkMovementMethod.getInstance());
			room.setFocusable(false);
			room.setFocusableInTouchMode(false);
			
			return v;
		}
	}

}