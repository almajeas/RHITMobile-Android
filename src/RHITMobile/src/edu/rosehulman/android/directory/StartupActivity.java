package edu.rosehulman.android.directory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import edu.rosehulman.android.directory.MyApplication.UpdateServiceListener;

public class StartupActivity extends Activity {
	
	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, StartupActivity.class);
		return intent;
	}
	
	private BetaManagerManager betaManager;
	
	private ArrayAdapter<Task> taskAdapter;
	
	private GridView tasksView;
	
	private static final int REQUEST_STARTUP_CODE = 4;
	
	private boolean updateData = true;
	private static IDataUpdateService updateService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		
        betaManager = new BetaManagerManager(this);
        
        if (betaManager.hasBetaManager()) {
        	//Add the beta channel
        	Task[] tasks = new Task[this.tasks.length+1];
        	for (int i = 0; i < this.tasks.length; i++) {
				tasks[i] = this.tasks[i];
			}
        	tasks[this.tasks.length] = new Task(
        			"Beta",
        			android.R.drawable.ic_menu_manage,
        			new OnClickListener() {
						@Override
						public void onClick(View v) {
							taskBeta_clicked();
						}
					});
        	this.tasks = tasks;
        }
		
		taskAdapter = new ArrayAdapter<Task>(
				this, 
				R.layout.startup_task, 
				R.id.task_label, 
				tasks) {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = inflater.inflate(R.layout.startup_task, null);
				
				TextView name = (TextView)v.findViewById(R.id.task_label);
				ImageView icon = (ImageView)v.findViewById(R.id.task_image);
				
				name.setText(tasks[position].name);
				icon.setImageResource(tasks[position].image);
				
				return v;
			}
		};
		
		tasksView = (GridView)findViewById(R.id.tasks);
		tasksView.setAdapter(taskAdapter);
		tasksView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				tasks[position].listener.onClick(v);
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
		
		MyApplication.getInstance().getDataUpdateService(new UpdateServiceListener() {
			@Override
			public void onServiceAcquired(IDataUpdateService service) {
				updateService = (IDataUpdateService)service;
				if (updateData) {
					updateService.startUpdate();
				}
			}
			
			@Override
			public void onServiceLost() {
				updateService = null;
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (updateService != null) {
			updateService.abort();
		}
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
    			updateData = true;
    			if (updateService != null) {
    				updateService.startUpdate();
    			}
    			break;	
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
	
	private void taskBeta_clicked() {
		betaManager.launchBetaActivity(BetaManagerManager.ACTION_SHOW_BETA_MANAGER);
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
	
	private Task[] tasks = new Task[] {
			new Task("Campus Map",
					android.R.drawable.ic_menu_mapmode,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					taskMap_clicked();
				}
			}), 
			new Task("Directory",
					android.R.drawable.ic_menu_send,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					taskDirectory_clicked();
				}
			}), 
			new Task("Campus Services",
					android.R.drawable.ic_menu_slideshow,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					taskServices_clicked();
				}
			}),
			new Task("My Schedule",
					android.R.drawable.ic_menu_my_calendar,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = PersonScheduleActivity.createIntent(StartupActivity.this, "Kevin");
					startActivity(intent);
				}
			})
		};
	

}
