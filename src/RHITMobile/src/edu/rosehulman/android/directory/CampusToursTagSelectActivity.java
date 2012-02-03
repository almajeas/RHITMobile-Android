package edu.rosehulman.android.directory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.rosehulman.android.directory.db.TourTagsAdapter;
import edu.rosehulman.android.directory.model.TourTag;
import edu.rosehulman.android.directory.model.TourTagsGroup;

public class CampusToursTagSelectActivity extends Activity {
	
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
	
	private ListView tags;
	
	private TourTagsGroup root;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour_tag_select_list);
		
		tags = (ListView)findViewById(R.id.tags);
		
		tags.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
				if (adapter == null)
					return;
				
				Intent intent = getIntent();
				String path = intent.getStringExtra(EXTRA_PATH);
				if (position < root.children.length){
					TourTagsGroup child = root.children[position];
					if ("".equals(path))
						path = child.name;
					else
						path += "/" + child.name;
					startActivityForResult(createIntent(CampusToursTagSelectActivity.this, child.id, path), 1);
				} else {
					TourTag tag = root.tags[position-root.children.length];
					Intent data = CampusToursTagListActivity.createResultIntent(tag, path);
					setResult(RESULT_OK, data);
					finish();
				}
			}
		});
		
    	handleIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		tags.setAdapter(null);
		handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		/* TODO search
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
    	*/
		
		if (!intent.hasExtra(EXTRA_ROOT_ID))
			return;
		
		long id = intent.getLongExtra(EXTRA_ROOT_ID, -1);
		if (id == -1)
			return;
		
		runSearch(id, "");
	}

	private void runSearch(long parent, String query) {
		LoadServices loadServices = new LoadServices(parent, query);
		taskManager.addTask(loadServices);
		loadServices.execute();
		
		//TODO set to something more useful
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
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		
		setResult(resultCode, data);
		finish();
	}
    
	private class LoadServices extends AsyncTask<Void, Void, TourTagsGroup> {
		
		private ProgressDialog dialog;
		
		private long rootId;
		private String query;
		
		public LoadServices(long rootId, String query) {
			this.rootId = rootId;
			this.query = query;
		}

		@Override
		protected TourTagsGroup doInBackground(Void... args) {
			
			TourTagsAdapter tagsAdapter = new TourTagsAdapter();
			tagsAdapter.open();
			TourTagsGroup res = tagsAdapter.getGroup(rootId, query);
			tagsAdapter.close();
			
			return res;
		}
		
		@Override
		protected void onPostExecute(TourTagsGroup res) {
			root = res;
			tags.setAdapter(new TagGroupAdapter(res));
			
			//TODO expand groups (if only one category and no tags match, dive into it)
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
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(CampusToursTagSelectActivity.this);
			View v;
			if (position < offset) {
				TourTagsGroup child = root.children[position];
				v = inflater.inflate(R.layout.tour_tag_select_group, null);
				TextView name = (TextView)v.findViewById(R.id.name);
				name.setText(child.name);
			} else {
				TourTag tag = root.tags[position-offset];
				v = inflater.inflate(R.layout.tour_tag_select_item, null);
				TextView name = (TextView)v.findViewById(R.id.name);
				name.setText(tag.name);
			}
			return v;
		}
	}

}
