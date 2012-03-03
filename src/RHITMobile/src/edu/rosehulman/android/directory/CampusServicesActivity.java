package edu.rosehulman.android.directory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.rosehulman.android.directory.IDataUpdateService.AsyncRequest;
import edu.rosehulman.android.directory.ServiceManager.ServiceRunnable;
import edu.rosehulman.android.directory.db.CampusServicesAdapter;
import edu.rosehulman.android.directory.model.CampusServicesCategory;
import edu.rosehulman.android.directory.model.Hyperlink;

public class CampusServicesActivity extends SherlockListActivity {

	public static final String EXTRA_ROOT_ID = "ROOT_ID";
	
	public static Intent createIntent(Context context) {
		return new Intent(context, CampusServicesActivity.class);
	}
	
	private static Intent createIntent(Context context, long rootId) {
		Intent intent = new Intent(context, CampusServicesActivity.class);
		intent.putExtra(EXTRA_ROOT_ID, rootId);
		return intent;
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private CampusServicesCategory category;

	private ServiceManager<IDataUpdateService> updateService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
		setContentView(R.layout.campus_services);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		updateService = new ServiceManager<IDataUpdateService>(getApplicationContext(),
				DataUpdateService.createIntent(getApplicationContext()));
		
    	updateService.run(new ServiceRunnable<IDataUpdateService>() {
			@Override
			public void run(IDataUpdateService service) {
				
				final ProgressDialog dialog = new ProgressDialog(CampusServicesActivity.this);
				service.requestCampusServices(new AsyncRequest() {
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

				    	handleIntent(getIntent());
					}
				});
			}
		});
	}
	
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	if (category == null)
			return;
		
		if (position < category.children.length) {
			CampusServicesCategory child = category.children[position];
			Intent intent = createIntent(CampusServicesActivity.this, child.id);
			startActivity(intent);
		} else {
			Hyperlink link = category.entries[position-category.children.length];
			String earl = link.url;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(earl));
			startActivity(intent);
		}
	}
	
	private void handleIntent(Intent intent) {
		long rootId = intent.getLongExtra(EXTRA_ROOT_ID, -1);
		LoadServices loadServices = new LoadServices(rootId);
		taskManager.addTask(loadServices);
		loadServices.execute();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		taskManager.abortTasks();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.campus_services, menu);
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
        case R.id.search:
        	onSearchRequested();
        	break;
        case android.R.id.home:
        	startActivity(StartupActivity.createIntent(this, true));
        	break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }
	
    private class ResultsAdapter extends BaseAdapter {
    	
		public CampusServicesCategory category;
		
		public ResultsAdapter(CampusServicesCategory category) {
			this.category = category;
		}

		@Override
		public int getCount() {
			return category.children.length + category.entries.length;
		}

		@Override
		public Object getItem(int position) {
			if (position < category.children.length){
				return category.children[position];
			} else {
				return category.entries[position-category.children.length];
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public int getViewTypeCount() {
			return 2;
		}
		
		@Override
		public int getItemViewType(int position) {
			if (position < category.children.length){
				return 0;
			} else {
				return 1;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View root = convertView;
			
			if (position < category.children.length){
				if (root == null) {
					LayoutInflater inflater = LayoutInflater.from(CampusServicesActivity.this);
					root = inflater.inflate(R.layout.service_group, null);
				}
				TextView name = (TextView)root.findViewById(R.id.name);
				
				CampusServicesCategory child = category.children[position];
				
				name.setText(child.name);
				
			} else {
				
				if (root == null) {
					LayoutInflater inflater = LayoutInflater.from(CampusServicesActivity.this);
					root = inflater.inflate(R.layout.service_child, null);
				}
				TextView name = (TextView)root.findViewById(R.id.name);
				
				Hyperlink link = category.entries[position-category.children.length];
				
				name.setText(link.name);
			}
			
			return root;
		}
    	
    }
	
	private class LoadServices extends BackgroundTask<Void, Void, CampusServicesCategory> {
		
		private long rootId;
		private String path;
		
		public LoadServices(long rootId) {
			this.rootId = rootId;
		}

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected CampusServicesCategory doInBackground(Void... args) {
			CampusServicesAdapter adapter = new CampusServicesAdapter();
			adapter.open();
			CampusServicesCategory category = adapter.getCategory(rootId);
			path = adapter.getPath(rootId, true);
			adapter.close();
			
			return category;
		}

		@Override
		protected void onAbort() {
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		protected void onCancelled() {
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
		@Override
		protected void onPostExecute(CampusServicesCategory res) {
			setSupportProgressBarIndeterminateVisibility(false);
			
			if (res == null) {
				finish();
				return;
			}
			
			category = res;
			setListAdapter(new ResultsAdapter(category));
			
			if (!"".equals(path)) {
				getSupportActionBar().setSubtitle(path);
			}
		}
		
	}

}
