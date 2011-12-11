package edu.rosehulman.android.directory;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

public class PersonScheduleActivity extends TabActivity {
	
	public static final String EXTRA_PERSON = "PERSON";
	
	public static Intent createIntent(Context context, String person) {
		Intent intent = new Intent(context, PersonScheduleActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		return intent;
	}
	
	private TabHost tabHost;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_schedule);
	
		
		//TabHost tabHost = getTabHost();
		tabHost = (TabHost)findViewById(android.R.id.tabhost);
		createTab("mon", "Mon");
		createTab("tue", "Tue");
		createTab("wed", "Wed");
		createTab("thu", "Thu");
		createTab("fri", "Fri");
		
		//TODO don't just redirect to a webpage
		String earl = "https://prodweb.rose-hulman.edu/regweb-cgi/reg-sched.pl?termcode=201220&view=tgrid&id1=wellska1&bt1=ID%2Username";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(earl));
		startActivity(intent);
		//finish();
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
			View v = inflater.inflate(R.layout.schedule_table, null);
			
			TextView name = (TextView)v.findViewById(R.id.tag);
			
			name.setText(tag);
			
			return v;
		}
	};
}
