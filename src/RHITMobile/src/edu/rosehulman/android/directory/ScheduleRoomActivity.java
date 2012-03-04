package edu.rosehulman.android.directory;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.TermCodeProvider.OnTermSetListener;
import edu.rosehulman.android.directory.model.RoomScheduleDay;
import edu.rosehulman.android.directory.model.RoomScheduleItem;
import edu.rosehulman.android.directory.model.RoomScheduleWeek;
import edu.rosehulman.android.directory.model.TermCode;

public class ScheduleRoomActivity extends FragmentActivity implements OnTermSetListener {
	
	public static final String EXTRA_ROOM = "ROOM";
	
	public static Intent createIntent(Context context, String room) {
		Intent intent = new Intent(context, ScheduleRoomActivity.class);
		intent.putExtra(EXTRA_ROOM, room);
		return intent;
	}

	private String room;
	
	private TaskManager taskManager = new TaskManager();
	
	private RoomScheduleWeek schedule;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_room);
		
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
		getSupportFragmentManager().beginTransaction().add(new AuthenticatedFragment(), "auth").commit();
		
		Intent intent = getIntent();
		if (!intent.hasExtra(EXTRA_ROOM)) {
			finish();
			return;
		}
		room = intent.getStringExtra(EXTRA_ROOM);
		
		if (savedInstanceState != null &&
				savedInstanceState.containsKey("Schedule")) {
			processSchedule((RoomScheduleWeek)savedInstanceState.getParcelable("Schedule"));
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
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.schedule_room, menu);
		menu.findItem(R.id.term).setActionProvider(new TermCodeProvider(this));
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
		String text = newTerm.code + " selected";
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	private void createTab(String tag, String label) {
		ActionBar actionBar = getSupportActionBar();
		Bundle args = ScheduleRoomFragment.buildArguments(tag, schedule.getDay(tag));
		TabListener<ScheduleRoomFragment> l = new TabListener<ScheduleRoomFragment>(this, tag, ScheduleRoomFragment.class, args);
		Tab tab = actionBar.newTab()
				.setText(label)
				.setTabListener(l);
		actionBar.addTab(tab);
		//TODO set selected day to today
	}
	
	private void processSchedule(RoomScheduleWeek res) {
		schedule = res;
		for (String day : res.tags) {
			createTab(day, day);
		}
		getSupportActionBar().setSubtitle(room);
	}
	
	private class LoadSchedule extends AsyncTask<Void, Void, RoomScheduleWeek> {

		@Override
		protected RoomScheduleWeek doInBackground(Void... params) {
			
			RoomScheduleItem csse432 = 
					new RoomScheduleItem("CSSE432", "Computer Networks", 1, 5, 5);
			RoomScheduleItem csse404 = 
					new RoomScheduleItem("CSSE404", "Compiler Construction", 1, 7, 7);
			RoomScheduleItem csse304 = 
					new RoomScheduleItem("CSSE304", "Programming Language Concepts", 1, 8, 8);
			
			
			RoomScheduleItem csse404Wed = 
					new RoomScheduleItem("CSSE404", "Compiler Construction", 1, 6, 6);
			RoomScheduleItem csse499Wed = 
					new RoomScheduleItem("CSSE499", "Senior Project III", 1, 7, 9);
			
			return new RoomScheduleWeek(
					new String[] {"Mon", "Tue", "Wed", "Thu", "Fri"}, 
					new RoomScheduleDay[] {
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse432, csse404, csse304
							}),
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse432, csse404, csse304
							}),
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse404Wed, csse499Wed
							}),
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse432, csse404, csse304
							}),
							new RoomScheduleDay(new RoomScheduleItem[] {
									csse432, csse304
							})
					});
		}
		
		@Override
		protected void onPostExecute(RoomScheduleWeek res) {
			processSchedule(res);
		}
		
	}
	
}
