package edu.rosehulman.android.directory;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.rosehulman.android.directory.model.Course;
import edu.rosehulman.android.directory.model.CourseMeeting;
import edu.rosehulman.android.directory.model.CoursesResponse;
import edu.rosehulman.android.directory.model.PersonScheduleDay;
import edu.rosehulman.android.directory.model.PersonScheduleItem;
import edu.rosehulman.android.directory.model.PersonScheduleWeek;
import edu.rosehulman.android.directory.model.ScheduleDay;
import edu.rosehulman.android.directory.model.TermCode;
import edu.rosehulman.android.directory.service.AuthenticationException;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

public class SchedulePersonActivity extends SherlockFragmentActivity implements TermCodeProvider.OnTermSetListener, AuthenticatedFragment.AuthenticationCallbacks {
	
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
	
	private AuthenticatedFragment fragAuth;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.schedule_person);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        
        FragmentManager fragments = getSupportFragmentManager();
        fragAuth = (AuthenticatedFragment)fragments.findFragmentByTag("auth");
        if (fragAuth == null) {
        	fragAuth = new AuthenticatedFragment();
			getSupportFragmentManager().beginTransaction().add(fragAuth, "auth").commit();
        }
		
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
	protected void onResume() {
		super.onResume();
		
		if (savedInstanceState != null &&
				savedInstanceState.containsKey("Schedule")) {
			processSchedule((PersonScheduleWeek)savedInstanceState.getParcelable("Schedule"));
			getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("Selected"));
			setSupportProgressBarIndeterminateVisibility(false);
			
		} else {
			if (!fragAuth.hasAuthToken()) {
				fragAuth.obtainAuthToken();
			}
		}
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
	
	private void createTab(ScheduleDay day, String tag, String label) {
		ActionBar actionBar = getSupportActionBar();
		Bundle args = SchedulePersonFragment.buildArguments(term, tag, schedule.getDay(day));
		TabListener<SchedulePersonFragment> l = new TabListener<SchedulePersonFragment>(this, tag, SchedulePersonFragment.class, args);
		Tab tab = actionBar.newTab()
				.setText(label)
				.setTabListener(l);
		actionBar.addTab(tab);
		//TODO set selected day to today
	}
	
	private void processSchedule(PersonScheduleWeek res) {
		schedule = res;
		String[] tags = getResources().getStringArray(R.array.schedule_days);
		for (ScheduleDay day : ScheduleDay.values()) {
			if (schedule.hasDay(day)) {
				String tag = tags[day.ordinal()];
				createTab(day, tag, tag);
			}
		}
		getSupportActionBar().setSubtitle(person);
	}
	
	@Override
	public void onAuthTokenObtained(String authtoken) {
		LoadSchedule task = new LoadSchedule(authtoken);
		taskManager.addTask(task);
		task.execute();
	}

	@Override
	public void onAuthTokenCancelled() {
		finish();
	}
	
	private class LoadSchedule extends AsyncTask<Void, Void, PersonScheduleWeek> {
		
		private boolean invalidAuthToken;
		private String authToken;
		private String errorMessage;
		
		public LoadSchedule(String authToken) {
			this.authToken = authToken;
		}
		
		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}
		
		private CoursesResponse loadCourses(MobileDirectoryService service) {
			while (true) {
				if (isCancelled())
					return null;
				
				try {
					Log.d(C.TAG, "Retrieving schedule");
					return service.getUserSchedule(authToken, person);

				} catch (AuthenticationException e) {
					invalidAuthToken = true;
					Log.w(C.TAG, "Invalid auth token");
					return null;
					
				} catch (ClientException e) {
					Log.e(C.TAG, "Client request failed", e);
					errorMessage = "Invalid request";
					return null;
					
				} catch (ServerException e) {
					Log.e(C.TAG, "Server request failed", e);
					errorMessage = "Service is rejecting requests. Please try again later.";
					return null;
					
				} catch (JSONException e) {
					Log.e(C.TAG, "An error occured while parsing the JSON response", e);
					errorMessage = "Service is rejecting requests. Please try again later.";
					return null;
					
				} catch (IOException e) {
					Log.e(C.TAG, "Network error, retrying...", e);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException ex) {
						return null;
					}
				}
			}
		}

		@Override
		protected PersonScheduleWeek doInBackground(Void... params) {
			Log.d(C.TAG, "Starting background task");
			
			//get the person's course schedules
			MobileDirectoryService service = new MobileDirectoryService();
			CoursesResponse response = loadCourses(service);
			
			if (response == null) {
				return null;
			}
			
			//convert the course schedules to a user schedule
			PersonScheduleWeek schedule = new PersonScheduleWeek();
			
			for (Course course : response.courses) {
				for (CourseMeeting meeting : course.schedule) {
					PersonScheduleDay day = schedule.getDay(meeting.day);
					PersonScheduleItem item;
					item = new PersonScheduleItem(course.crn, course.course, course.title, meeting.startPeriod, meeting.endPeriod, meeting.room);
					day.addItem(item);
				}
			}
			
			return schedule;
		}
		
		@Override
		protected void onPostExecute(PersonScheduleWeek res) {
			if (res == null) {
				if (errorMessage != null) {
    				Toast.makeText(SchedulePersonActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    			}
				if (invalidAuthToken) {
					fragAuth.invalidateAuthToken(authToken);
					fragAuth.obtainAuthToken();
				} else {
					finish();
				}
				return;
			}
			
			setSupportProgressBarIndeterminateVisibility(false);
			processSchedule(res);
		}
		
	}
}
