package edu.rosehulman.android.directory;

import java.util.LinkedList;
import java.util.List;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.db.DbIterator;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.fragments.AuthenticatedFragment;
import edu.rosehulman.android.directory.fragments.EnableGpsDialogFragment;
import edu.rosehulman.android.directory.fragments.ObtainLocationDialogFragment;
import edu.rosehulman.android.directory.loaders.InvalidAuthTokenException;
import edu.rosehulman.android.directory.loaders.LoadRoomSchedule;
import edu.rosehulman.android.directory.loaders.LoaderException;
import edu.rosehulman.android.directory.loaders.LoaderResult;
import edu.rosehulman.android.directory.model.Hyperlink;
import edu.rosehulman.android.directory.model.LatLon;
import edu.rosehulman.android.directory.model.LightLocation;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.RoomScheduleWeek;
import edu.rosehulman.android.directory.tasks.LoadLocation;
import edu.rosehulman.android.directory.tasks.TaskManager;
import edu.rosehulman.android.directory.tasks.UITask;

public class LocationActivity extends SherlockFragmentActivity implements ObtainLocationDialogFragment.LocationCallbacks, EnableGpsDialogFragment.EnableGpsCallbacks, AuthenticatedFragment.AuthenticationCallbacks {

	public static final String EXTRA_LOCATION = "LOCATION";

    public static Intent createIntent(Context context, Location location) {
		Intent intent = new Intent(context, LocationActivity.class);
		intent.putExtra(LocationActivity.EXTRA_LOCATION, location);
		return intent;
    }
    
	private TaskManager taskManager;
    
    private View header;
    private TextView description;
    private ListView details;

    private Location location;
    private LightLocation[] children;

	private AuthenticatedFragment mFragAuth;
	
	private RoomScheduleWeek mSchedule;
	private View btnSchedule;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        taskManager = new TaskManager();
        
        header = LayoutInflater.from(this).inflate(R.layout.location_header, null);
        description = (TextView)header.findViewById(R.id.description);
        
        View btnShowOnMap = header.findViewById(R.id.btnShowOnMap);
        View btnDirections = header.findViewById(R.id.btnDirections);
        btnSchedule = header.findViewById(R.id.btnSchedule);
        
        details = (ListView)findViewById(R.id.details);
        details.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				ListAdapter adapter = details.getAdapter();
				
				if (adapter == null)
					return;
				
				ListItems.ListItem item = (ListItems.ListItem)adapter.getItem(position);
				details_itemClicked(item);
			}
		});
        details.addHeaderView(header);
        
        btnShowOnMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnShowOnMap_clicked();
			}
		});
        btnDirections.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnDirections_clicked();
			}
		});
        btnSchedule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnSchedule_clicked();
			}
		});
         
        if (User.isLoggedIn(AccountManager.get(this))) {
            btnSchedule.setVisibility(View.VISIBLE);
            btnSchedule.setEnabled(false);
        	
    		FragmentManager fragments = getSupportFragmentManager();
            mFragAuth = (AuthenticatedFragment)fragments.findFragmentByTag("auth");
            if (mFragAuth == null) {
            	mFragAuth = new AuthenticatedFragment();
    			getSupportFragmentManager().beginTransaction().add(mFragAuth, "auth").commit();
            }

            LoaderManager loaderManager = getSupportLoaderManager();
            if (loaderManager.getLoader(TASK_LOAD_SCHEDULE) != null) {
            	loaderManager.initLoader(TASK_LOAD_SCHEDULE, null, mLoadScheduleCallbacks);
            }
        }
        
        location = getIntent().getParcelableExtra(EXTRA_LOCATION);
        
        if (savedInstanceState == null) {
        	   
	    } else {
	    	//restore state
	    }
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	processResult();
    	
        if (mFragAuth != null) {
        	mFragAuth.obtainAuthToken();
        }
    	
    	LoadLocationExtras loadExtras = new LoadLocationExtras();
    	taskManager.addTask(loadExtras);
    	loadExtras.execute(location);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        
        //stop any tasks we were running
        taskManager.abortTasks();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle item selection
        switch (item.getItemId()) {
        case android.R.id.home:
        	finish();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void processResult() {
        ListItems.DetailsAdapter adapter = createDetailsAdapter();
        
        setTitle(location.name);
        description.setText(location.description);
        
        details.setAdapter(adapter);
    }
    
    private ListItems.DetailsAdapter createDetailsAdapter() {
    	List<ListItems.ListItem> items = new LinkedList<ListItems.ListItem>();
    	
    	if (location.links.length > 0) {
    		items.add(new ListItems.ListHeader(this, getString(R.string.links)));
    		
    		for (Hyperlink link : location.links) {
    			//TODO filter based on link type
    			items.add(new HyperlinkItem(this, link));
    		}
    	}
    	
    	if (children != null && children.length > 0) {
    		items.add(new ListItems.ListHeader(this, getString(R.string.whats_inside)));
    		
    		for (LightLocation child : children) {
    			items.add(new InnerLocationItem(this, child));
    		}
    	}
    	
    	return new ListItems.DetailsAdapter(items);
    }
    
    private void details_itemClicked(ListItems.ListItem item) {
    	item.onClick();
    }
    
    private void btnShowOnMap_clicked() {
    	Intent intent = CampusMapActivity.createIntent(this, location.id);
    	startActivity(intent);
    }
    
    private void btnDirections_clicked() {
    	final CharSequence[] locations = {"Inside", "Outside"};

    	new AlertDialog.Builder(this)
    		.setTitle("Where are you? ")
    		.setItems(locations, new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	    	    	switch (item) {
	    	    	case 0: //inside
	    	    		new UITask<String, Long>() {

							@Override
							public void getInput(int attempt) {
								if (attempt > 0) {
									Toast.makeText(LocationActivity.this, "Location not found. Try again.", Toast.LENGTH_SHORT).show();
								}
								
								LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
								View root = inflater.inflate(R.layout.text_entry_dialog, null);
								final EditText roomView = (EditText)root.findViewById(R.id.edit);
			    	    		new AlertDialog.Builder(LocationActivity.this)
			    	    			.setTitle("What room are you near?")
			    	    			.setView(root)
			    	    			.setPositiveButton("Get Directions", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											String room = roomView.getText().toString();
											setInput(room);
										}
									})
									.setOnCancelListener(new DialogInterface.OnCancelListener() {
										@Override
										public void onCancel(DialogInterface dialog) {
											cancel();
										}
									})
									.setCancelable(true)
			    	    			.show();
							}

							@Override
							public void processInput(String name) {
								LocationAdapter locationAdapter = new LocationAdapter();
								locationAdapter.open();
								
								long id = locationAdapter.findLocation(name);
								
								locationAdapter.close();
								
								if (id >= 0)
									setResult(id);
							}

							@Override
							public void taskCompleted(Long res) {
								Intent intent = CampusMapActivity.createDirectionsIntent(LocationActivity.this, res, location.id);
								startActivity(intent);
							}

	    	    		}.start();
	    	    		
	    	    		break;
	    	    	case 1: //outside
	    	    	{
	    	    		new ObtainLocationDialogFragment().show(getSupportFragmentManager(), ObtainLocationDialogFragment.TAG);
	    	    	} break;
	    	    	}
	    	    }
	    	})
	    	.show();
    }
    
    private void btnSchedule_clicked() {
    	Intent intent = ScheduleRoomActivity.createIntent(this, location.name, mSchedule);
    	startActivity(intent);
    }

    private class HyperlinkItem extends ListItems.ClickableListItem {
    	
    	private Hyperlink mLink;

		public HyperlinkItem(Context context, Hyperlink link) {
			super(context, link.name, link.url);
			mLink = link;
		}

		@Override
		public void onClick() {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(mLink.url));
			startActivity(intent);
		}
    }
    
    private class InnerLocationItem extends ListItems.ClickableListItem {
    	
    	private LightLocation mChild;

		public InnerLocationItem(Context context, LightLocation child) {
			super(context, child.name, null);
			mChild = child;
		}

		@Override
		public void onClick() {
			LoadLocation loadChild = new LoadLocation(mChild.id, new LoadLocation.OnLocationLoadedListener() {
				@Override
				public void onLocationLoaded(Location location) {
					Intent intent = createIntent(LocationActivity.this, location);
					LocationActivity.this.startActivity(intent);
				}
			});
			taskManager.addTask(loadChild);
			loadChild.execute();
		}
    }
	
	private class LoadLocationExtras extends AsyncTask<Location, Void, LightLocation[]> {

		@Override
		protected LightLocation[] doInBackground(Location... params) {
			Location loc = params[0];
			
			LocationAdapter locationAdapter = new LocationAdapter();
			locationAdapter.open();
			
			DbIterator<LightLocation> locations = locationAdapter.getChildren(loc.id);
			
			LightLocation[] children = new LightLocation[locations.getCount()];
			for (int i = 0; locations.hasNext(); i++) {
				children[i] = locations.getNext();
			}
			
			locationAdapter.close();
			return children;
		}
		
		@Override
		protected void onPostExecute(LightLocation[] res) {
			LocationActivity.this.children = res;
			
			processResult();
		}
		
	}

	@Override
	public void onLocationObtained(LatLon loc) {
		Intent intent = CampusMapActivity.createDirectionsIntent(this, loc, location.id);
		startActivity(intent);
	}
	
	@Override
	public void onLocationCancelled() {
		
	}

	@Override
	public void onEnableGpsTriggered() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.remove(getSupportFragmentManager().findFragmentByTag(ObtainLocationDialogFragment.TAG));
		ft.commit();
	}

	@Override
	public void onAuthTokenObtained(String authToken) {
		loadSchedule(authToken);
	}

	@Override
	public void onAuthTokenCancelled() {
		
	}
	
	private void onScheduleObtained(RoomScheduleWeek schedule) {
		if (schedule == null || schedule.isEmpty())
			return;
		
		mSchedule = schedule;
		btnSchedule.setEnabled(true);
	}

	private static final int TASK_LOAD_SCHEDULE = 1;
	private void loadSchedule(String authToken) {
		Bundle args = LoadRoomSchedule.bundleArgs(authToken, User.getTerm().code, location.name);
		getSupportLoaderManager().initLoader(TASK_LOAD_SCHEDULE, args, mLoadScheduleCallbacks);
	}
	
	private LoaderCallbacks<LoaderResult<RoomScheduleWeek>> mLoadScheduleCallbacks = new LoaderCallbacks<LoaderResult<RoomScheduleWeek>>() {

		private Handler mHandler = new Handler();
		
		@Override
		public Loader<LoaderResult<RoomScheduleWeek>> onCreateLoader(int id, Bundle args) {
			return new LoadRoomSchedule(LocationActivity.this, args);
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
						onScheduleObtained(result);
					}
				});
				
			} catch (InvalidAuthTokenException ex) {
				LoadRoomSchedule scheduleLoader = (LoadRoomSchedule)loader;
				mFragAuth.invalidateAuthToken(scheduleLoader.getAuthToken());
				mFragAuth.obtainAuthToken();
				
			} catch (LoaderException ex) {
				String message = ex.getMessage();
				if (message != null) {
					Toast.makeText(LocationActivity.this, message, Toast.LENGTH_SHORT).show();
				}
			}	
		}

		@Override
		public void onLoaderReset(Loader<LoaderResult<RoomScheduleWeek>> loader) {
		}
	};
}
