package edu.rosehulman.android.directory;

import java.util.List;

import android.app.ProgressDialog;
import android.app.SearchManager;
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

import edu.rosehulman.android.directory.IDataUpdateService.AsyncRequest;
import edu.rosehulman.android.directory.LoadCampusServiceHyperlink.OnHyperlinkLoadedListener;
import edu.rosehulman.android.directory.ServiceManager.ServiceRunnable;
import edu.rosehulman.android.directory.db.CampusServicesAdapter;
import edu.rosehulman.android.directory.model.CampusServiceItem;
import edu.rosehulman.android.directory.model.Hyperlink;

public class CampusServicesSearchActivity extends SherlockListActivity {

	public static Intent createIntent(Context context) {
		return new Intent(context, CampusServicesSearchActivity.class);
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private CampusServiceItem[] items;

	private ServiceManager<IDataUpdateService> updateService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.campus_services_search);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		
		updateService = new ServiceManager<IDataUpdateService>(getApplicationContext(),
				DataUpdateService.createIntent(getApplicationContext()));
		
    	updateService.run(new ServiceRunnable<IDataUpdateService>() {
			@Override
			public void run(IDataUpdateService service) {
				
				final ProgressDialog dialog = new ProgressDialog(CampusServicesSearchActivity.this);
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
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    		runSearch(intent.getStringExtra(SearchManager.QUERY));
    		
    	} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
		    Uri data = intent.getData();
		    
		    long id = Long.parseLong(data.getPath());
		    LoadCampusServiceHyperlink task = new LoadCampusServiceHyperlink(new OnHyperlinkLoadedListener() {
				
				@Override
				public void onLinkLoaded(Hyperlink link) {
					if (link == null)
						return;
					
					finish();
					Intent newIntent = new Intent(Intent.ACTION_VIEW);
					newIntent.setData(Uri.parse(link.url));
					startActivity(newIntent);
				}
			}); 
		    taskManager.addTask(task);
		    task.execute(id);
		    
    	} else {
    		//are you lost?
    		setResult(RESULT_CANCELED);
    		finish();
    	}
	}

	private void runSearch(String query) {
		LoadServices loadServices = new LoadServices(query);
		taskManager.addTask(loadServices);
		loadServices.execute();
		
		if (!"".equals(query)) {
			getSupportActionBar().setSubtitle(query);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
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
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	if (items == null)
			return;
		
    	CampusServiceItem item = items[position];

		Hyperlink link = item.link;
		String earl = link.url;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(earl));
		startActivity(intent);
	}
	
    private class ResultsAdapter extends BaseAdapter {
    	
    	public CampusServiceItem[] items;
		
		public ResultsAdapter(CampusServiceItem[] items) {
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int position) {
			return items[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			
			View root = convertView;
			
			if (root == null) {
				LayoutInflater inflater = LayoutInflater.from(CampusServicesSearchActivity.this);
				root = inflater.inflate(R.layout.service_search_item, null);
			}
			
			TextView name = (TextView)root.findViewById(R.id.name);
			TextView path = (TextView)root.findViewById(R.id.path);
			
			CampusServiceItem item = items[position];
			name.setText(item.link.name);
			path.setText(item.path);
			
			return root;
		}
    	
    }
    
	private class LoadServices extends BackgroundTask<Void, Void, CampusServiceItem[]> {
		
		private ProgressDialog dialog;
		private String query;
		
		public LoadServices(String query) {
			this.query = query;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusServicesSearchActivity.this);
			dialog.setTitle(null);
			dialog.setMessage("Loading...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			dialog.show();
		}

		@Override
		protected CampusServiceItem[] doInBackground(Void... args) {
			CampusServicesAdapter adapter = new CampusServicesAdapter();
			adapter.open();
			List<CampusServiceItem> items = adapter.search(query).toList();
			CampusServiceItem[] res = new CampusServiceItem[items.size()];
			items.toArray(res);
			adapter.close();
			
			return res;
		}

		@Override
		protected void onAbort() {
			dialog.dismiss();
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
		}
		
		@Override
		protected void onPostExecute(CampusServiceItem res[]) {
			dialog.dismiss();
			
			items = res;
			setListAdapter(new ResultsAdapter(items));
		}
		
	}

}
