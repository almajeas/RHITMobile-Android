package edu.rosehulman.android.directory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.rosehulman.android.directory.loaders.AsyncLoaderException;
import edu.rosehulman.android.directory.loaders.AsyncLoaderResult;
import edu.rosehulman.android.directory.loaders.InvalidAuthTokenException;
import edu.rosehulman.android.directory.loaders.LoadSchedule;
import edu.rosehulman.android.directory.model.PersonScheduleWeek;
import edu.rosehulman.android.directory.model.ScheduleDay;
import edu.rosehulman.android.directory.model.TermCode;

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
	
	private static final int TASK_LOAD_SCHEDULE = 1;
	
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
		setSupportProgressBarIndeterminateVisibility(true);
		fragAuth.obtainAuthToken();
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
			fragAuth.obtainAuthToken();
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
		
		//cleanup old schedule
		ActionBar actionBar = getSupportActionBar();
		actionBar.removeAllTabs();
		((FrameLayout)findViewById(R.id.fragment_content)).removeAllViews();
		
		//populate new schedule, if there is one
		if (schedule == null) {
			getSupportActionBar().setSubtitle(null);
			return;
		}
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
	public void onAuthTokenObtained(String authToken) {
		reloadSchedule(authToken);
	}

	@Override
	public void onAuthTokenCancelled() {
		Toast.makeText(this, getString(R.string.authentication_error), Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private void reloadSchedule(String authToken) {
		Bundle args = LoadSchedule.bundleArgs(authToken, person);
		getSupportLoaderManager().restartLoader(TASK_LOAD_SCHEDULE, args, mLoadScheduleCallbacks);
	}
	
	private LoaderCallbacks<AsyncLoaderResult<PersonScheduleWeek>> mLoadScheduleCallbacks = new LoaderCallbacks<AsyncLoaderResult<PersonScheduleWeek>>() {

		private Handler mHandler = new Handler();
		
		@Override
		public Loader<AsyncLoaderResult<PersonScheduleWeek>> onCreateLoader(int id, Bundle args) {
			return new LoadSchedule(SchedulePersonActivity.this, args);
		}

		@Override
		public void onLoadFinished(Loader<AsyncLoaderResult<PersonScheduleWeek>> loader, AsyncLoaderResult<PersonScheduleWeek> data) {
			
			try {
				final PersonScheduleWeek result = data.getResult();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setSupportProgressBarIndeterminateVisibility(false);
						processSchedule(result);	
					}
				});
				
			} catch (InvalidAuthTokenException ex) {
				LoadSchedule scheduleLoader = (LoadSchedule)loader;
				fragAuth.invalidateAuthToken(scheduleLoader.getAuthToken());
				fragAuth.obtainAuthToken();
				
			} catch (AsyncLoaderException ex) {
				String message = ex.getMessage();
				if (message != null) {
					Toast.makeText(SchedulePersonActivity.this, message, Toast.LENGTH_SHORT).show();
				}
				finish();
			}	
		}

		@Override
		public void onLoaderReset(Loader<AsyncLoaderResult<PersonScheduleWeek>> loader) {
			processSchedule(null);
		}
	};
}
