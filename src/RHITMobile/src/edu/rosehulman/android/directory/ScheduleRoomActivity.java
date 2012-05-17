package edu.rosehulman.android.directory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
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

import edu.rosehulman.android.directory.fragments.AuthenticatedFragment;
import edu.rosehulman.android.directory.fragments.ScheduleRoomFragment;
import edu.rosehulman.android.directory.loaders.LoaderException;
import edu.rosehulman.android.directory.loaders.LoaderResult;
import edu.rosehulman.android.directory.loaders.InvalidAuthTokenException;
import edu.rosehulman.android.directory.loaders.LoadRoomSchedule;
import edu.rosehulman.android.directory.model.RoomScheduleWeek;
import edu.rosehulman.android.directory.model.ScheduleDay;
import edu.rosehulman.android.directory.model.TermCode;

public class ScheduleRoomActivity extends SherlockFragmentActivity implements TermCodeProvider.OnTermSetListener, AuthenticatedFragment.AuthenticationCallbacks {
	
	public static final String EXTRA_ROOM = "Room";
	public static final String EXTRA_SCHEDULE = "Schedule";
	public static final String EXTRA_TERM_CODE = "TermCode";

	private static final String STATE_SELECTED = "Selected";
	
	public static Intent createIntent(Context context, String room) {
		Intent intent = new Intent(context, ScheduleRoomActivity.class);
		intent.putExtra(EXTRA_ROOM, room);
		return intent;
	}

	public static Intent createIntent(Context context, String room, RoomScheduleWeek schedule) {
		Intent intent = new Intent(context, ScheduleRoomActivity.class);
		intent.putExtra(EXTRA_ROOM, room);
		intent.putExtra(EXTRA_SCHEDULE, schedule);
		return intent;
	}
	
	public static Intent createIntent(Context context, String room, TermCode term) {
		Intent intent = new Intent(context, ScheduleRoomActivity.class);
		intent.putExtra(EXTRA_ROOM, room);
		intent.putExtra(EXTRA_TERM_CODE, term);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}
	
	private static final int TASK_LOAD_SCHEDULE = 1;
	
	private String room;
	private TermCode term;
	
	private RoomScheduleWeek schedule;
	
	private Bundle savedInstanceState;
	
	private AuthenticatedFragment fragAuth;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.schedule_room);

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
		
		LoaderManager loaders = getSupportLoaderManager();
		if (LoadRoomSchedule.getInstance(loaders, TASK_LOAD_SCHEDULE) != null) {
			loaders.initLoader(TASK_LOAD_SCHEDULE, null, mLoadScheduleCallbacks);
		}
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

		if (!intent.hasExtra(EXTRA_ROOM)) {
			finish();
			return;
		}
		room = intent.getStringExtra(EXTRA_ROOM);
		
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

		state.putInt(STATE_SELECTED, getSupportActionBar().getSelectedNavigationIndex());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fragAuth.obtainAuthToken();
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

		Intent intent = createIntent(this, room, term);
		startActivity(intent);
	}
	
	private void createTab(ScheduleDay day, String tag, String label) {
		ActionBar actionBar = getSupportActionBar();
		Bundle args = ScheduleRoomFragment.buildArguments(term, tag, schedule.getDay(day));
		TabListener<ScheduleRoomFragment> l = new TabListener<ScheduleRoomFragment>(this, tag, ScheduleRoomFragment.class, args);
		Tab tab = actionBar.newTab()
				.setText(label)
				.setTabListener(l);

		actionBar.addTab(tab);
		if (ScheduleDay.today() == day) {
			actionBar.selectTab(tab);
		}
	}
	
	private void processSchedule(RoomScheduleWeek res) {
		//cleanup old schedule
		ActionBar actionBar = getSupportActionBar();
		actionBar.removeAllTabs();
		String[] tags = getResources().getStringArray(R.array.schedule_days);
		if (schedule != null) {
			FragmentManager fragments = getSupportFragmentManager();
			FragmentTransaction ft = fragments.beginTransaction();
			for (ScheduleDay day : ScheduleDay.values()) {
				if (schedule.hasDay(day)) {
					String tag = tags[day.ordinal()];
					Fragment frag = fragments.findFragmentByTag(tag);
					if (frag != null) {
						ft.remove(frag);
					}
				}
			}
			ft.commit();
			fragments.executePendingTransactions();
		}
		
		//populate new schedule, if there is one
		schedule = res;
		if (schedule == null) {
			actionBar.setSubtitle(null);
			return;
		}
		
		for (ScheduleDay day : ScheduleDay.values()) {
			if (schedule.hasDay(day)) {
				String tag = tags[day.ordinal()];
				createTab(day, tag, tag);
			}
		}
		actionBar.setSubtitle(room);
		
		if (savedInstanceState != null) {
			int index = savedInstanceState.getInt(STATE_SELECTED);
			if (index >= 0 && index < actionBar.getTabCount()) {
				actionBar.setSelectedNavigationItem(index);
			}
		}
	}
	
	private Handler mHandler = new Handler();
	@Override
	public void onAuthTokenObtained(final String authToken) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Intent intent = getIntent();
				if (intent.hasExtra(EXTRA_SCHEDULE)) {
					setSupportProgressBarIndeterminateVisibility(false);
					processSchedule((RoomScheduleWeek)intent.getParcelableExtra(EXTRA_SCHEDULE));
				} else {
					loadSchedule(authToken);
				}	
			}
		});
	}

	@Override
	public void onAuthTokenCancelled() {
		Toast.makeText(this, getString(R.string.authentication_error), Toast.LENGTH_SHORT).show();
		finish();
	}
	
	private Bundle mArgs;
	private void loadSchedule(String authToken) {
		Bundle args = LoadRoomSchedule.bundleArgs(authToken, term.code, room);
		
		if (mArgs != null) {
			getSupportLoaderManager().restartLoader(TASK_LOAD_SCHEDULE, args, mLoadScheduleCallbacks);
		} else {
			getSupportLoaderManager().initLoader(TASK_LOAD_SCHEDULE, args, mLoadScheduleCallbacks);
		}
		
		mArgs = args;
	}
	
	private LoaderCallbacks<LoaderResult<RoomScheduleWeek>> mLoadScheduleCallbacks = new LoaderCallbacks<LoaderResult<RoomScheduleWeek>>() {

		private Handler mHandler = new Handler();
		
		@Override
		public Loader<LoaderResult<RoomScheduleWeek>> onCreateLoader(int id, Bundle args) {
			return new LoadRoomSchedule(ScheduleRoomActivity.this, args);
		}

		@Override
		public void onLoadFinished(Loader<LoaderResult<RoomScheduleWeek>> loader, LoaderResult<RoomScheduleWeek> data) {
			Log.d(C.TAG, "Finished LoadRoomSchedule");
			
			try {
				final RoomScheduleWeek result = data.getResult();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						setSupportProgressBarIndeterminateVisibility(false);
						processSchedule(result);	
					}
				});
				
			} catch (InvalidAuthTokenException ex) {
				LoadRoomSchedule scheduleLoader = (LoadRoomSchedule)loader;
				fragAuth.invalidateAuthToken(scheduleLoader.getAuthToken());
				fragAuth.obtainAuthToken();
				
			} catch (LoaderException ex) {
				String message = ex.getMessage();
				if (message != null) {
					Toast.makeText(ScheduleRoomActivity.this, message, Toast.LENGTH_SHORT).show();
				}
				finish();
			}	
		}

		@Override
		public void onLoaderReset(Loader<LoaderResult<RoomScheduleWeek>> loader) {
		}
	};
}