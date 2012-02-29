package edu.rosehulman.android.directory;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import edu.rosehulman.android.directory.model.TourTagsGroup;

public class CampusToursTagSelectActivity extends SherlockListActivity {
	
	public static final String EXTRA_PATH = "PATH";
	public static final String EXTRA_ROOT_ID = "ROOT_ID";
	
	public static Intent createIntent(Context context, long rootId) {
		return createIntent(context, rootId, "");
	}
	
	private static Intent createIntent(Context context, long rootId, String path) {
		Intent intent = new Intent(context, CampusToursTagSelectActivity.class);
		intent.putExtra(EXTRA_ROOT_ID, rootId);
		intent.putExtra(EXTRA_PATH, path);
		return intent;
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private TourTagsGroup root;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour_tag_select_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
		
    	handleIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		setListAdapter(null);
		handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		
		long parentId = intent.getLongExtra(EXTRA_ROOT_ID, -1);

		if (parentId == -1)
			return;
		
		LoadServices loadServices = new LoadServices(parentId);
		taskManager.addTask(loadServices);
		loadServices.execute();
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
        inflater.inflate(R.menu.tour_tag_select, menu);
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
        case android.R.id.home:
        	finish();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	if (root == null)
    		return;
    	
		Intent intent = getIntent();
		String path = intent.getStringExtra(EXTRA_PATH);
		if (position < root.children.length){
			TourTagsGroup child = root.children[position];
			if (path == null || "".equals(path))
				path = child.name;
			else
				path += "/" + child.name;
			Intent newIntent = createIntent(this, child.id, path);
			startActivity(newIntent);
		} else {
			TourTag tag = root.tags[position-root.children.length];
			Intent newIntent = CampusToursTagListActivity.createIntent(this, tag, path);
			startActivity(newIntent);
			finish();
		}
	}
    
	private class LoadServices extends AsyncTask<Void, Void, TourTagsGroup> {
		
		private long rootId;
		private String path;
		
		public LoadServices(long rootId) {
			this.rootId = rootId;
		}

		@Override
		protected TourTagsGroup doInBackground(Void... args) {
			
			TourTagsAdapter tagsAdapter = new TourTagsAdapter();
			tagsAdapter.open();
			TourTagsGroup res = tagsAdapter.getGroup(rootId);
			path = tagsAdapter.getPath(rootId, true);
			tagsAdapter.close();
			
			return res;
		}
		
		@Override
		protected void onPostExecute(TourTagsGroup res) {
			root = res;
			setListAdapter(new TagGroupAdapter(res));
			
			if (!"".equals(path)) {
				getSupportActionBar().setSubtitle(path);
			}
		}
		
	}
	
	private class TagGroupAdapter extends BaseAdapter {
		
		private TourTagsGroup root;
		private int offset;
		
		public TagGroupAdapter(TourTagsGroup root) {
			this.root = root;
			this.offset = root.children.length;
		}

		@Override
		public int getCount() {
			return root.children.length + root.tags.length;
		}

		@Override
		public Object getItem(int position) {
			if (position < offset)
				return root.children[position];
			return root.tags[position-offset];
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
			if (position < offset) {
				return 0;
			} else {
				return 1;
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (position < offset) {
				TourTagsGroup child = root.children[position];
				if (v == null) {
					LayoutInflater inflater = LayoutInflater.from(CampusToursTagSelectActivity.this);
					v = inflater.inflate(R.layout.tour_tag_select_group, null);
				}
				TextView name = (TextView)v.findViewById(R.id.name);
				name.setText(child.name);
			} else {
				TourTag tag = root.tags[position-offset];
				if (v == null) {
					LayoutInflater inflater = LayoutInflater.from(CampusToursTagSelectActivity.this);
					v = inflater.inflate(R.layout.tour_tag_select_item, null);
				}
				TextView name = (TextView)v.findViewById(R.id.name);
				name.setText(tag.name);
			}
			return v;
		}
	}

}
