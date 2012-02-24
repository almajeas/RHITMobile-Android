package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.ServiceManager.ServiceRunnable;

public class StartupActivity extends SherlockActivity {
	
	public static Intent createIntent(Context context, boolean clearTop) {
		Intent intent = new Intent(context, StartupActivity.class);
		if (clearTop) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		}
		return intent;
	}
	
	private BetaManagerManager betaManager;
	
	private List<Task> tasks;
	
	private GridView tasksView;
	
	private static final int REQUEST_STARTUP_CODE = 4;

	private ServiceManager<IDataUpdateService> updateService;
	private boolean updateData = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		
        betaManager = new BetaManagerManager(this);
        
		tasksView = (GridView)findViewById(R.id.tasks);
		tasksView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
				if (adapter == null)
					return;
				
				tasks.get(position).listener.onClick(v);
			}
		});
        
		if (savedInstanceState == null) {
			if (betaManager.hasBetaManager() && betaManager.isBetaEnabled()) {
				updateData = false;
				if (betaManager.isBetaRegistered()) {
					Intent betaIntent = betaManager.getBetaIntent(BetaManagerManager.ACTION_SHOW_STARTUP);
					startActivityForResult(betaIntent, REQUEST_STARTUP_CODE);
				} else {
					betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_REGISTER);
				}
			}
		}
		
		updateService = new ServiceManager<IDataUpdateService>(getApplicationContext(),
				DataUpdateService.createIntent(getApplicationContext()));
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		updateService.run(new ServiceRunnable<IDataUpdateService>() {
			@Override
			public void run(IDataUpdateService service) {
				if (updateData) {
					service.startUpdate();
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		//populate the tasks
        updateUI();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		updateService.cancel();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		updateService.run(new ServiceRunnable<IDataUpdateService>() {
			@Override
			public void run(IDataUpdateService service) {
				service.abort();
			}
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		updateUI();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode != REQUEST_STARTUP_CODE)
    		return;
    	
    	switch (resultCode) {
    		case Activity.RESULT_CANCELED:
    			//The user declined an update, exit
    			finish();
    			break;
    		case Activity.RESULT_OK:
    			//We were up to date, continue on happily
    			updateService.run(new ServiceRunnable<IDataUpdateService>() {
    				@Override
    				public void run(IDataUpdateService service) {
    					service.startUpdate();
    				}
    			});
    			break;	
    	}
    }
    
    @Override
    public boolean onSearchRequested() {
    	if (User.isLoggedIn()) {
    		super.onSearchRequested();
    		return true;
    	}
    	return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.startup, menu);
        return true;
    }
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle item selection
        switch (item.getItemId()) {
        case R.id.preferences:
        	startActivity(PreferencesActivity.createIntent(this));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	private void taskMap_clicked() {
		Intent intent = CampusMapActivity.createIntent(this);
		startActivity(intent);
	}
	
	private void taskDirectory_clicked() {
		onSearchRequested();
	}
	
	private void taskServices_clicked() {
		Intent intent = CampusServicesActivity.createIntent(this);
		startActivity(intent);
	}

	private void taskTours_clicked() {
		Intent intent = CampusToursStartupActivity.createIntent(this);
		startActivity(intent);
	}
	
	private void taskBeta_clicked() {
		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_BETA_MANAGER);
	}
	
	private void updateUI() {
		tasks = new ArrayList<Task>();
		
		boolean loggedIn = User.isLoggedIn();
		
		tasks.add(
			new Task("Campus Map",
				R.drawable.homescreen_map,
				new OnClickListener() {
			@Override
			public void onClick(View v) {
				taskMap_clicked();
			}
		}));
		
		if (loggedIn) {
			tasks.add(
				new Task("Directory",
					R.drawable.homescreen_directory,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					taskDirectory_clicked();
				}
			}));
		}
		
		tasks.add(
			new Task("Campus Services",
				R.drawable.homescreen_campus_services,
				new OnClickListener() {
			@Override
			public void onClick(View v) {
				taskServices_clicked();
			}
		}));
		
		tasks.add(
			new Task("Tours",
				R.drawable.homescreen_tours,
				new OnClickListener() {
			@Override
			public void onClick(View v) {
				taskTours_clicked();
			}
		}));
		
		if (loggedIn) {
			tasks.add(
				new Task("My Schedule",
					R.drawable.homescreen_schedule,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = SchedulePersonActivity.createIntent(StartupActivity.this, User.getUsername());
					startActivity(intent);
				}
			}));
		}
		
		if (!loggedIn) {
			tasks.add(
				new Task("Login",
					R.drawable.homescreen_login,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = LoginActivity.createIntent(StartupActivity.this);
					startActivity(intent);
				}
			}));
		}
		
		if (betaManager.hasBetaManager()) {
        	//Add the beta channel
			tasks.add(
				new Task("Beta",
        			R.drawable.homescreen_beta,
        			new OnClickListener() {
						@Override
						public void onClick(View v) {
							taskBeta_clicked();
						}
					}));
        }
		
		tasksView.setAdapter(new TasksAdapter(tasks));
	}
	
	private class Task {
		public String name;
		public OnClickListener listener;
		public int image;
		
		public Task(String name, int image, OnClickListener listener) {
			this.name = name;
			this.image = image;
			this.listener = listener;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private class TasksAdapter extends BaseAdapter {
		
		private List<Task> tasks;

		public TasksAdapter(List<Task> tasks) {
			this.tasks = tasks;
		}

		@Override
		public int getCount() {
			return tasks.size();
		}

		@Override
		public Object getItem(int position) {
			return tasks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.startup_task, null);
			}
			Task task = tasks.get(position);
			
			TextView name = (TextView)v.findViewById(R.id.task_label);
			ImageView icon = (ImageView)v.findViewById(R.id.task_image);
			
			name.setText(task.name);
			icon.setImageResource(task.image);
			
			return v;
		}
		
		
	}

	

}
