package edu.rosehulman.android.directory;

import java.util.List;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.db.TourTagsAdapter;
import edu.rosehulman.android.directory.model.TourTag;
import edu.rosehulman.android.directory.model.TourTagItem;
import edu.rosehulman.android.directory.tasks.BackgroundTask;
import edu.rosehulman.android.directory.tasks.LoadTourTag;
import edu.rosehulman.android.directory.tasks.TaskManager;
import edu.rosehulman.android.directory.tasks.LoadTourTag.OnTourTagLoadedListener;

public class CampusToursTagSelectSearchActivity extends SherlockListActivity {

	public static Intent createIntent(Context context) {
		return new Intent(context, CampusToursTagSelectSearchActivity.class);
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private TourTagItem[] items;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour_tag_select_search);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		handleIntent(getIntent());
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
		    LoadTourTag task = new LoadTourTag(new OnTourTagLoadedListener() {

				@Override
				public void onTagLoaded(TourTag tag, String path) {
					if (tag == null) {
						setResult(RESULT_CANCELED);
						finish();
						return;
					}
					
					Intent intent = CampusToursTagListActivity.createIntent(CampusToursTagSelectSearchActivity.this, tag, path);
					startActivity(intent);
					finish();
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
		LoadSearch loadSearch = new LoadSearch(query);
		taskManager.addTask(loadSearch);
		loadSearch.execute();
		
		if (!"".equals(query)) {
			getSupportActionBar().setSubtitle(query);
		}
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
        	setResult(RESULT_CANCELED);
        	finish();
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
		
    	TourTagItem item = items[position];

    	Intent intent = CampusToursTagListActivity.createIntent(this, item.tag, item.path);
    	startActivity(intent);
    	finish();
	}
	
    private class ResultsAdapter extends BaseAdapter {
    	
    	public TourTagItem[] items;
		
		public ResultsAdapter(TourTagItem[] items) {
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
				LayoutInflater inflater = LayoutInflater.from(CampusToursTagSelectSearchActivity.this);
				root = inflater.inflate(R.layout.tour_tag_select_search_item, null);
			}
			
			TextView name = (TextView)root.findViewById(R.id.name);
			TextView path = (TextView)root.findViewById(R.id.path);
			
			TourTagItem item = items[position];
			name.setText(item.tag.name);
			path.setText(item.path);
			
			return root;
		}	
    }
    
	private class LoadSearch extends BackgroundTask<Void, Void, TourTagItem[]> {
		
		private ProgressDialog dialog;
		private String query;
		
		public LoadSearch(String query) {
			this.query = query;
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(CampusToursTagSelectSearchActivity.this);
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
		protected TourTagItem[] doInBackground(Void... args) {
			TourTagsAdapter adapter = new TourTagsAdapter();
			adapter.open();
			List<TourTagItem> items = adapter.search(query).toList();
			TourTagItem[] res = new TourTagItem[items.size()];
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
		protected void onPostExecute(TourTagItem res[]) {
			dialog.dismiss();
			
			items = res;
			setListAdapter(new ResultsAdapter(items));
		}
		
	}

}
