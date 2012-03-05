package edu.rosehulman.android.directory;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.rosehulman.android.directory.model.PersonScheduleDay;
import edu.rosehulman.android.directory.model.PersonScheduleItem;
import edu.rosehulman.android.directory.model.PersonScheduleWeek;
import edu.rosehulman.android.directory.model.TermCode;

public class SchedulePersonActivity extends FragmentActivity implements TermCodeProvider.OnTermSetListener {
	
	public static final String EXTRA_PERSON = "Person";
	public static final String EXTRA_TERM_CODE = "TermCode";
	
	public static Intent createIntent(Context context, String person) {
		Intent intent = new Intent(context, SchedulePersonActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		return intent;
	}

	public static Intent createIntent(Context context, String person, TermCode term) {
		Intent intent = new Intent(context, SchedulePersonActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		intent.putExtra(EXTRA_TERM_CODE, term);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}
	
	private String person;
	private TermCode term;
	
	private TaskManager taskManager = new TaskManager();
	
	private PersonScheduleWeek schedule;
	
	private Bundle savedInstanceState;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.schedule_person);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
		getSupportFragmentManager().beginTransaction().add(new AuthenticatedFragment(), "auth").commit();
		
		this.savedInstanceState = savedInstanceState;
        
		handleIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent newIntent) {
		super.onNewIntent(newIntent);
		setIntent(newIntent);
		this.savedInstanceState = null;
		handleIntent(newIntent);
	}
	
	private void handleIntent(Intent intent) {

		if (!intent.hasExtra(EXTRA_PERSON)) {
			finish();
			return;
		}
		person = intent.getStringExtra(EXTRA_PERSON);
		
		term = (TermCode)intent.getParcelableExtra(EXTRA_TERM_CODE);
		if (term == null) {
			term = User.getTerm();
		}
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.removeAllTabs();
		
		((FrameLayout)findViewById(R.id.fragment_content)).removeAllViews();
		
		if (savedInstanceState != null &&
				savedInstanceState.containsKey("Schedule")) {
			processSchedule((PersonScheduleWeek)savedInstanceState.getParcelable("Schedule"));
			getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("Selected"));
			setSupportProgressBarIndeterminateVisibility(false);
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
	protected void onPause() {
		super.onPause();
		taskManager.abortTasks();
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.schedule_person, menu);
		menu.findItem(R.id.term).setActionProvider(new TermCodeProvider(this, term));
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

	@Override
	public void onTermSet(TermCode newTerm) {
		if (newTerm.equals(term))
			return;
		
		term = newTerm;
		User.setTerm(term);

		Intent intent = createIntent(this, person, term);
		startActivity(intent);
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
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected PersonScheduleWeek doInBackground(Void... params) {
			
			if ("201210".equals(term.code)) {
				PersonScheduleItem csse432 = 
						new PersonScheduleItem("CSSE432", "Computer Networks", 1, 5, 5, "O205");
				PersonScheduleItem csse404 = 
						new PersonScheduleItem("CSSE404", "Compiler Construction", 1, 7, 7, "O267");

				return new PersonScheduleWeek(
						new String[] {"Mon", "Thu", "Fri"}, 
						new PersonScheduleDay[] {
								new PersonScheduleDay(new PersonScheduleItem[] {
										csse432, csse404
								}),
								new PersonScheduleDay(new PersonScheduleItem[] {
										csse432, csse404
								}),
								new PersonScheduleDay(new PersonScheduleItem[] {
										csse432
								})
						});
			}
			
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
			setSupportProgressBarIndeterminateVisibility(false);
			processSchedule(res);
		}
		
	}
}
