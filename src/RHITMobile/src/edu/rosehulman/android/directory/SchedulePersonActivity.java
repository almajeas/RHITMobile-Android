package edu.rosehulman.android.directory;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.ActionProvider;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.model.PersonScheduleDay;
import edu.rosehulman.android.directory.model.PersonScheduleItem;
import edu.rosehulman.android.directory.model.PersonScheduleWeek;
import edu.rosehulman.android.directory.model.TermCode;

public class SchedulePersonActivity extends FragmentActivity {
	
	public static final String EXTRA_PERSON = "PERSON";
	
	public static Intent createIntent(Context context, String person) {
		Intent intent = new Intent(context, SchedulePersonActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		return intent;
	}

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
		
		((FrameLayout)findViewById(R.id.fragment_content)).removeAllViews();
		
		if (savedInstanceState != null &&
				savedInstanceState.containsKey("Schedule")) {
			processSchedule((PersonScheduleWeek)savedInstanceState.getParcelable("Schedule"));
			getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("Selected"));
		} else {
			LoadSchedule task = new LoadSchedule();
			taskManager.addTask(task);
			task.execute();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		
		if (schedule != null) {
			state.putParcelable("Schedule", schedule);
		}
		
		state.putInt("Selected", getSupportActionBar().getSelectedNavigationIndex());
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Term")
			.setActionProvider(new TermCodeProvider(this))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
					MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
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
		Bundle args = SchedulePersonFragment.buildArguments(tag, schedule.getDay(tag));
		TabListener<SchedulePersonFragment> l = new TabListener<SchedulePersonFragment>(this, tag, SchedulePersonFragment.class, args);
		Tab tab = actionBar.newTab()
				.setText(label)
				.setTabListener(l);
		actionBar.addTab(tab);
		//TODO set selected day to today
	}
	
	private void processSchedule(PersonScheduleWeek res) {
		schedule = res;
		for (String day : res.tags) {
			createTab(day, day);
		}
		getSupportActionBar().setSubtitle(person);
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
			processSchedule(res);
		}
		
	}
	
	private class TermCodeProvider extends ActionProvider {
	
		private TermCode[] TERMS = new TermCode[] {
			new TermCode("201230", "Spring 2012"),
			new TermCode("201220", "Winter 2011"),
			new TermCode("201210", "Fall 2011"),
			new TermCode("201130", "Spring 2011"),
			new TermCode("201120", "Winter 2010"),
			new TermCode("201110", "Fall 2010"),
			new TermCode("201030", "Spring 2010"),
			new TermCode("201020", "Winter 2009"),
			new TermCode("201010", "Fall 2009"),
			new TermCode("200930", "Spring 2009"),
			new TermCode("200920", "Winter 2008"),
			new TermCode("200910", "Fall 2008")
		};
		
		private Context mContext;

		public TermCodeProvider(Context context) {
			super(context);
			mContext = context;
		}

		@Override
		public boolean onPerformDefaultAction() {
			return super.onPerformDefaultAction();
		}

		@Override
		public View onCreateActionView() {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			Spinner terms = (Spinner)inflater.inflate(R.layout.schedule_term_selector, null);
			
			ArrayAdapter<TermCode> termAdapter = new ArrayAdapter<TermCode>(mContext, R.layout.sherlock_spinner_item_light_dark, TERMS);
	        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        terms.setAdapter(termAdapter);
	        terms.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
					String text = TERMS[position].code + " selected";
					Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {}
			});
			
			return terms;
		}
		
	}
}
