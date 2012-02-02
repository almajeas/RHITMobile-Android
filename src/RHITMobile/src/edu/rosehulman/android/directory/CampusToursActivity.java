package edu.rosehulman.android.directory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import edu.rosehulman.android.directory.LoadCampusServiceHyperlink.OnHyperlinkLoadedListener;
import edu.rosehulman.android.directory.model.Hyperlink;
import edu.rosehulman.android.directory.model.TourTagsGroup;
import edu.rosehulman.android.directory.model.TourTagsResponse;
import edu.rosehulman.android.directory.service.MobileDirectoryService;

public class CampusToursActivity extends Activity {
	
	public static Intent createIntent(Context context) {
		return new Intent(context, CampusToursActivity.class);
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private TourTagView tagsView;
	
	private TourTagsGroup root;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.campus_tours);
		
		tagsView = (TourTagView)findViewById(R.id.tagList);
		
		/*tree.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (root == null)
					return false;
				
				//tree_childClicked(root[groupPosition].entries[childPosition]);
				return true;
			}
		});*/
		
    	handleIntent(getIntent());
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
			setTitle("Search: " + query);
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
        MenuInflater inflater = getMenuInflater();
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
	
    /*
	private void tree_childClicked(Hyperlink child) {
		String earl = child.url;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(earl));
		startActivity(intent);
	}
	*/
	
	private class LoadServices extends AsyncTask<Void, Void, TourTagsGroup> {
		
		private ProgressDialog dialog;
		private String query;
		
		public LoadServices(String query) {
			this.query = query;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusToursActivity.this);
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
		protected TourTagsGroup doInBackground(Void... args) {
			//TODO query service
			
			MobileDirectoryService service = new MobileDirectoryService();
			TourTagsResponse response;
			try {
				response = service.getTourTagData("");
			} catch (Exception e) {
				return null;
			}
			return response.root; 
			
			//TODO pull from db
			/*
			CampusServicesAdapter adapter = new CampusServicesAdapter();
			adapter.open();
			TourTagsGroup categories[] = adapter.getCategories(query);
			adapter.close();
			
			return categories;
			*/
		}
		
		@Override
		protected void onCancelled() {
			dialog.dismiss();
		}
		
		@Override
		protected void onPostExecute(TourTagsGroup res) {
			dialog.dismiss();
			
			root = res;
			tagsView.setData(res);
			
			//TODO expand groups
			/*
			if (query.length() > 0) {
				for (int i = 0; i < res.length; i++) {
					tree.expandGroup(i);
				}
			}
			*/
		}
		
	}

}
