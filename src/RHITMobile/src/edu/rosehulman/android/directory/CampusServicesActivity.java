package edu.rosehulman.android.directory;

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
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.IDataUpdateService.AsyncRequest;
import edu.rosehulman.android.directory.LoadCampusServiceHyperlink.OnHyperlinkLoadedListener;
import edu.rosehulman.android.directory.ServiceManager.ServiceRunnable;
import edu.rosehulman.android.directory.db.CampusServicesAdapter;
import edu.rosehulman.android.directory.model.CampusServicesCategory;
import edu.rosehulman.android.directory.model.Hyperlink;

public class CampusServicesActivity extends SherlockListActivity {
	
	public static Intent createIntent(Context context) {
		return new Intent(context, CampusServicesActivity.class);
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private ExpandableListView tree;
	
	private CampusServicesCategory[] categories;

	private ServiceManager<IDataUpdateService> updateService;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.campus_services);
		
		tree = (ExpandableListView)findViewById(android.R.id.list);
		
		tree.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (categories == null)
					return false;
				
				tree_childClicked(categories[groupPosition].entries[childPosition]);
				return true;
			}
		});
		
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
	protected void onNewIntent(Intent intent) {
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
					finish();
					Intent newIntent = new Intent(Intent.ACTION_VIEW);
					newIntent.setData(Uri.parse(link.url));
					startActivity(newIntent);
				}
			}); 
		    taskManager.addTask(task);
		    task.execute(id);
    	} else {
    		runSearch("");	
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
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	private void tree_childClicked(Hyperlink child) {
		String earl = child.url;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(earl));
		startActivity(intent);
	}
	
	
	private class TreeAdapter extends BaseExpandableListAdapter {
		
		public CampusServicesCategory[] groups;
		
		public TreeAdapter(CampusServicesCategory[] groups) {
			this.groups = groups;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			CampusServicesCategory group = groups[groupPosition];
			
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.service_group, null);
			
			TextView name = (TextView)v.findViewById(R.id.name);
			
			name.setText(group.name);
			
			return v;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			Hyperlink child = groups[groupPosition].entries[childPosition];
			
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.service_child, null);
			
			TextView name = (TextView)v.findViewById(R.id.name);
			
			name.setText(child.name);
			
			return v;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groups[groupPosition];
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return groups[groupPosition].entries[childPosition];
		}

		@Override
		public int getGroupCount() {
			return groups.length;
		}
		
		@Override
		public int getChildrenCount(int groupPosition) {
			return groups[groupPosition].entries.length;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
		
	}
	
	private class LoadServices extends BackgroundTask<Void, Void, CampusServicesCategory[]> {
		
		private ProgressDialog dialog;
		private String query;
		
		public LoadServices(String query) {
			this.query = query;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusServicesActivity.this);
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
		protected CampusServicesCategory[] doInBackground(Void... args) {
			CampusServicesAdapter adapter = new CampusServicesAdapter();
			adapter.open();
			CampusServicesCategory categories[] = adapter.getCategories(query);
			adapter.close();
			
			return categories;
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
		protected void onPostExecute(CampusServicesCategory res[]) {
			dialog.dismiss();
			
			categories = res;
			tree.setAdapter(new TreeAdapter(categories));
			
			if (query.length() > 0) {
				for (int i = 0; i < res.length; i++) {
					tree.expandGroup(i);
				}
			}
		}
		
	}

}
