package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.db.TourTagsAdapter;
import edu.rosehulman.android.directory.model.TourTag;
import edu.rosehulman.android.directory.model.TourTagItem;

public class CampusToursTagListActivity extends SherlockActivity {
	
    private static int REQUEST_TAG = 10;
    
    public static String EXTRA_START_LOCATION = "START_LOCATION";
    public static String EXTRA_INITIAL_TAGS = "INITIAL_TAGS";
	public static String EXTRA_TAG = "TAG";
	public static String EXTRA_TAG_PATH = "TAG_PATH";
	
	public static Intent createIntent(Context context, TourTagItem[] tags) {
		Intent intent = new Intent(context, CampusToursTagListActivity.class);
		if (tags != null) {
			intent.putExtra(EXTRA_INITIAL_TAGS, tags);
		}
		return intent;
	}
	
	public static Intent createIntent(Context context, long startLocation, TourTagItem[] tags) {
		Intent intent = new Intent(context, CampusToursTagListActivity.class);
		intent.putExtra(EXTRA_START_LOCATION, startLocation);
		if (tags != null) {
			intent.putExtra(EXTRA_INITIAL_TAGS, tags);
		}
		return intent;
	}
	
	public static Intent createResultIntent(TourTag tag, String path) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_TAG, tag);
		intent.putExtra(EXTRA_TAG_PATH, path);
		return intent;
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private TouchListView tags;
	
	private List<TourTagItem> tagItems = new ArrayList<TourTagItem>();
	
	private TagsAdapter adapter = new TagsAdapter();
	
	private Button btnTour;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tour_tag_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		tags = (TouchListView)findViewById(R.id.tags);
		
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

		Intent intent = getIntent();
		if (savedInstanceState == null) {
			if (intent.hasExtra(EXTRA_INITIAL_TAGS)) {
				for (Parcelable p : intent.getParcelableArrayExtra(EXTRA_INITIAL_TAGS)) {
					TourTagItem tag = (TourTagItem)p;
					tagItems.add(tag);
				}
			}	
		} else {
			for (Parcelable p : savedInstanceState.getParcelableArray(EXTRA_INITIAL_TAGS)) {
				TourTagItem tag = (TourTagItem)p;
				tagItems.add(tag);
			}
		}
		
		tags.setAdapter(adapter);
		tags.setDropListener(adapter);
		tags.setRemoveListener(adapter);
		
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
	protected void onSaveInstanceState(Bundle outState) {
		TourTagItem[] items = tagItems.toArray(new TourTagItem[tagItems.size()]);
		outState.putParcelableArray(EXTRA_INITIAL_TAGS, items);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_TAG)
			return;
		
		if (resultCode != RESULT_OK)
			return;
		
		TourTag tag = data.getParcelableExtra(EXTRA_TAG);
		TourTagItem item = new TourTagItem(tag, data.getStringExtra(EXTRA_TAG_PATH));
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

		long[] tagIds = new long[tagItems.size()];
		for (int i = 0; i < tagItems.size(); i++) {
			tagIds[i] = tagItems.get(i).tag.tagId;
		}
		
    	if (intent.hasExtra(EXTRA_START_LOCATION)) {
    		//on campus (inside) tour
    		long startId = intent.getLongExtra(EXTRA_START_LOCATION, -1);
    		if (startId < 0) {
    			finish();
    			return;
    		}
    		
    		Intent newIntent = CampusMapActivity.createTourIntent(this, startId, tagIds);
    		startActivity(newIntent);
    	} else {
    		//off campus tour
    		Intent newIntent = CampusMapActivity.createTourIntent(this, tagIds);
    		startActivity(newIntent);
    	}
    }
    
    
    private class TagsAdapter extends BaseAdapter implements TouchListView.DropListener, TouchListView.RemoveListener {

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
			return tagItems.get(index).tag.tagId;
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = LayoutInflater.from(CampusToursTagListActivity.this).inflate(R.layout.tour_tag_list_item, null);
			}
			
			TextView name = (TextView)v.findViewById(R.id.name);
			TextView path = (TextView)v.findViewById(R.id.path);
			
			TourTagItem item = tagItems.get(index);
			
			name.setText(item.tag.name);
			path.setText(item.path);
			
			return v;
		}

		@Override
		public void remove(int which) {
			tagItems.remove(which);
			updateUI();
		}

		@Override
		public void drop(int from, int to) {
			tagItems.add(to, tagItems.remove(from));
			tags.invalidateViews();
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
