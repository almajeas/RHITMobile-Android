package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import edu.rosehulman.android.directory.db.TourTagsAdapter;
import edu.rosehulman.android.directory.model.TourTag;

public class CampusToursTagListActivity extends Activity {
	
    private static int REQUEST_TAG = 10;
    
    public static String EXTRA_START_LOCATION = "START_LOCATION";
	public static String EXTRA_TAG = "TAG";
	public static String EXTRA_TAG_PATH = "TAG_PATH";
	
	public static Intent createIntent(Context context) {
		return new Intent(context, CampusToursTagListActivity.class);
	}
	
	public static Intent createIntent(Context context, long startLocation) {
		Intent intent = new Intent(context, CampusToursTagListActivity.class);
		intent.putExtra(EXTRA_START_LOCATION, startLocation);
		return intent;
	}
	
	public static Intent createResultIntent(TourTag tag, String path) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_TAG, tag);
		intent.putExtra(EXTRA_TAG_PATH, path);
		return intent;
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private ListView tags;
	
	private List<TagItem> tagItems = new ArrayList<TagItem>();
	
	private TagsAdapter adapter = new TagsAdapter();
	
	private Button btnTour;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour_tag_list);
		
		tags = (ListView)findViewById(R.id.tags);
		
		btnTour = (Button)findViewById(R.id.btnTour);
		
		findViewById(R.id.btnAdd).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAdd_clicked();
			}
		});
		
		btnTour.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnTour_clicked();
			}
		});
		
		tags.setAdapter(adapter);
		updateUI();
	}
	
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
		if (requestCode != REQUEST_TAG)
			return;
		
		if (resultCode != RESULT_OK)
			return;
		
		TourTag tag = data.getParcelableExtra(EXTRA_TAG);
		TagItem item = new TagItem(tag, data.getStringExtra(EXTRA_TAG_PATH));
		if (!tagItems.contains(item))
			tagItems.add(item);
		updateUI();
	}
    
    private void updateUI() {
		tags.requestLayout();
		btnTour.setEnabled(!tagItems.isEmpty());
    }
    
    private void btnAdd_clicked() {
    	AddTagTask task = new AddTagTask();
    	taskManager.addTask(task);
    	task.execute();
    }
    
    private void btnTour_clicked() {
    	Intent intent = getIntent();
    	
    	if (intent.hasExtra(EXTRA_START_LOCATION)) {
    		long startId = intent.getLongExtra(EXTRA_START_LOCATION, -1);
    		if (startId < 0) {
    			finish();
    			return;
    		}
    		
    		long[] tagIds = new long[tagItems.size()];
    		for (int i = 0; i < tagItems.size(); i++) {
				tagIds[i] = tagItems.get(i).tag.id;
			}
    		
    		Intent newIntent = CampusMapActivity.createTourIntent(this, startId, tagIds);
    		startActivity(newIntent);
    	}
    	
    	//TODO implement
    }
    
    private class TagItem {
    	
    	public TourTag tag;
    	public String path;
    	
		public TagItem(TourTag tag, String path) {
			this.tag = tag;
			this.path = path;
		}
		
		public boolean equals(TagItem o) {
			return o.tag.equals(tag) &&
					o.path.equals(path);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof TagItem)
				return equals((TagItem)o);
			return false;
		}
    }

    private class TagsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return tagItems.size();
		}

		@Override
		public Object getItem(int index) {
			return tagItems.get(index);
		}

		@Override
		public long getItemId(int index) {
			return tagItems.get(index).tag.id;
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = LayoutInflater.from(CampusToursTagListActivity.this).inflate(R.layout.tour_tag_list_item, null);
			}
			
			TextView name = (TextView)v.findViewById(R.id.name);
			TextView path = (TextView)v.findViewById(R.id.path);
			
			TagItem item = tagItems.get(index);
			
			name.setText(item.tag.name);
			path.setText(item.path);
			
			return v;
		}
    	
    }
	
    private class AddTagTask extends AsyncTask<Void, Void, Long> {

		@Override
		protected Long doInBackground(Void... params) {
			TourTagsAdapter adapter = new TourTagsAdapter();
			adapter.open();
			long res = adapter.getRootId();
			adapter.close();
			
			return res;
		}
    	
		@Override
		protected void onPostExecute(Long res) {
			Intent intent = CampusToursTagSelectActivity.createIntent(CampusToursTagListActivity.this, res);
	    	startActivityForResult(intent, REQUEST_TAG);
		}
    }

}
