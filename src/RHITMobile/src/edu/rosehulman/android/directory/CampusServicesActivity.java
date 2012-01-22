package edu.rosehulman.android.directory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import edu.rosehulman.android.directory.db.CampusServicesAdapter;
import edu.rosehulman.android.directory.model.CampusServicesCategory;
import edu.rosehulman.android.directory.model.Hyperlink;

public class CampusServicesActivity extends Activity {
	
	public static Intent createIntent(Context context) {
		return new Intent(context, CampusServicesActivity.class);
	}
	
	private TaskManager taskManager = new TaskManager();
	
	private ExpandableListView tree;
	
	private CampusServicesCategory[] categories;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.campus_services);
		
		tree = (ExpandableListView)findViewById(R.id.tree);
		
		LoadServices loadServices = new LoadServices();
		taskManager.addTask(loadServices);
		loadServices.execute();
		
		tree.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				if (categories == null)
					return false;
				
				tree_childClicked(categories[groupPosition].entries[childPosition]);
				return true;
			}
		});
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
	
	private class LoadServices extends AsyncTask<Void, Void, CampusServicesCategory[]> {
		
		private ProgressDialog dialog;
		
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
			//TODO query service
			
			CampusServicesAdapter adapter = new CampusServicesAdapter();
			adapter.open();
			CampusServicesCategory categories[] = adapter.getCategories("");
			adapter.close();
			
			return categories;
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
		}
		
	}

}
