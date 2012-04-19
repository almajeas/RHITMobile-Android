package edu.rosehulman.android.directory;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.IDataUpdateService.AsyncRequest;
import edu.rosehulman.android.directory.ServiceManager.ServiceRunnable;
import edu.rosehulman.android.directory.db.LocationAdapter;
import edu.rosehulman.android.directory.db.TourTagsAdapter;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.TourTag;
import edu.rosehulman.android.directory.model.TourTagItem;

public class CampusToursStartupActivity extends SherlockActivity {
	
	public static Intent createIntent(Context context) {
		return new Intent(context, CampusToursStartupActivity.class);
	}
	
	private Button btnTour;
	
	private RadioGroup groupLocation;
	private RadioButton rdoOnCampusInside;
	private RadioButton rdoOnCampusOutside;
	private RadioButton rdoOffCampus;
	
	private RadioGroup groupType;
	private RadioButton rdoGeneral;
	private RadioButton rdoCustom;
	
	private TaskManager taskManager = new TaskManager();
	private TourTagItem[] defaultTags;
	
	private ServiceManager<IDataUpdateService> updateService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour_startup);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
		
		groupLocation = (RadioGroup)findViewById(R.id.groupLocation);
		rdoOnCampusInside = (RadioButton)findViewById(R.id.rdoOnCampusInside);
		rdoOnCampusOutside = (RadioButton)findViewById(R.id.rdoOnCampusOutside);
		rdoOffCampus = (RadioButton)findViewById(R.id.rdoOffCampus);
		
		groupType = (RadioGroup)findViewById(R.id.groupType);
		rdoGeneral= (RadioButton)findViewById(R.id.rdoGeneral);
		rdoCustom = (RadioButton)findViewById(R.id.rdoCustom);
		
		btnTour = (Button)findViewById(R.id.btnTour);
		
		groupLocation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				enableButton();
			}
		});

		rdoOnCampusInside.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				rdoOnCampusInside_checked(isChecked);
			}
		});

		rdoOnCampusOutside.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				rdoOnCampusOutside_checked(isChecked);
			}
		});

		rdoOffCampus.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				rdoOffCampus_checked(isChecked);
			}
		});
		
		groupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				enableButton();
			}
		});

		btnTour.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnTour_clicked();
			}
		});
		
		updateService = new ServiceManager<IDataUpdateService>(getApplicationContext(),
				DataUpdateService.createIntent(getApplicationContext()));
	}
	
	public void onResume() {
		super.onResume();

    	updateService.run(new ServiceRunnable<IDataUpdateService>() {

			@Override
			public void run(IDataUpdateService service) {
				
				final ProgressDialog dialog = new ProgressDialog(CampusToursStartupActivity.this);
				service.requestTourTags(new AsyncRequest() {
					boolean isCancelled = false;	
					@Override
					public void onQueued(Runnable cancelCallback) {
						dialog.setTitle("");
						dialog.setMessage("Loading...");
						dialog.setIndeterminate(true);
						dialog.setCancelable(true);
						dialog.setOnCancelListener(new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								isCancelled = true;
								finish();
							}
						});
						dialog.show();
					}
					
					@Override
					public void onCompleted() {
						dialog.dismiss();
						if (isCancelled)
							return;
						
						LoadDefaultTags task = new LoadDefaultTags();
						taskManager.addTask(task);
						task.execute();
					}
				});
			}
		});
	}
	
	@Override
	public void onPause() {
		super.onPause();
		taskManager.abortTasks();
		updateService.cancel();
	}
	
    @Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		
		//TODO save/restore inside room state
		//TODO save/restore outside position state
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
	
	private Location startLocation;

	private void rdoOnCampusInside_checked(boolean isChecked) {
		if (!isChecked) {
			rdoOnCampusInside.setText("On Campus (inside)");
			return;
		}
		
		startLocation = null;
		new UITask<String, Location>() {

			@Override
			public void getInput(int attempt) {
				if (attempt > 0) {
					Toast.makeText(CampusToursStartupActivity.this, "Location not found. Try again.", Toast.LENGTH_SHORT).show();
				}
				
				LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				View root = inflater.inflate(R.layout.text_entry_dialog, null);
				final EditText roomView = (EditText)root.findViewById(R.id.edit);
	    		new AlertDialog.Builder(CampusToursStartupActivity.this)
	    			.setTitle("What room are you near?")
	    			.setView(root)
	    			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String room = roomView.getText().toString();
							setInput(room);
						}
					})
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							groupLocation.clearCheck();
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
				
				try {
					long id = locationAdapter.findLocation(name);
					if (id < 0)
						return;
					
					Location loc = locationAdapter.getLocation(id);
					setResult(loc);
					
				} finally {
					locationAdapter.close();
				}
			}

			@Override
			public void taskCompleted(Location res) {
				rdoOnCampusInside.setText("On Campus (near " + res.name + ")");
				startLocation = res;
			}

		}.start();		
	}
	
	private void rdoOnCampusOutside_checked(boolean isChecked) {
		//TODO start/stop GPS

	}
	
	private void rdoOffCampus_checked(boolean isChecked) {
	}
	
    private void btnTour_clicked() {
    	if (rdoOnCampusOutside.isChecked()) {
    		Toast.makeText(this, "Outside tours not yet supported", Toast.LENGTH_SHORT).show();
    		groupLocation.clearCheck();
    		return;
    	}
    	
    	if (rdoGeneral.isChecked()) {
    		//general tours
       		if (rdoOffCampus.isChecked()) {
    			Intent intent = CampusMapActivity.createTourIntent(this, TourTagItem.getIds(defaultTags));
    			startActivity(intent);
    		} else {
	    		Intent intent = CampusMapActivity.createTourIntent(this, startLocation.id, TourTagItem.getIds(defaultTags));
	    		startActivity(intent);
    		}
    	} else {
    		//special interest tours
    		if (rdoOffCampus.isChecked()) {
    			Intent intent = CampusToursTagListActivity.createIntent(this, defaultTags);
    			startActivity(intent);
    		} else {
	    		Intent intent = CampusToursTagListActivity.createIntent(this, startLocation.id, defaultTags);
	    		startActivity(intent);
    		}
    	}
    }
    
    private void enableButton() {
    	btnTour.setEnabled(((rdoOnCampusInside.isChecked() || rdoOnCampusOutside.isChecked() || rdoOffCampus.isChecked()) &&
    			(rdoGeneral.isChecked() || rdoCustom.isChecked()) &&
    			defaultTags != null));
    }
    
    private class LoadDefaultTags extends AsyncTask<Void, Void, TourTagItem[]> {

		@Override
		protected TourTagItem[] doInBackground(Void... args) {
			TourTagsAdapter tagsAdapter = new TourTagsAdapter();
			tagsAdapter.open();
			List<TourTag> tagList = tagsAdapter.getDefaultTags().toList();
			
			TourTag[] tags = new TourTag[tagList.size()];
			tagList.toArray(tags);

			TourTagItem[] res = new TourTagItem[tags.length];
			for (int i = 0; i < res.length; i++) {
				String path = tagsAdapter.getTagPath(tags[i].tagId);
				res[i] = new TourTagItem(tags[i], path);
			}
			tagsAdapter.close();
			
			return res;
		}
		
		@Override
		protected void onPostExecute(TourTagItem[] res) {
			defaultTags = res;
			enableButton();
		}
    	
    }
}
