package edu.rosehulman.android.directory;

import android.app.Activity;
import android.content.Context;
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

public class CampusServicesActivity extends Activity {
	
	public static Intent createIntent(Context context) {
		return new Intent(context, CampusServicesActivity.class);
	}
	
	private ExpandableListView tree;
	
	private Group[] groups = new Group[] {
			new Group("Career Services", new Child[] {
					new Child("Contacts", "http://www.rose-hulman.edu/careerservices/contacts.htm"),
					new Child("eRecruiting", "http://rhit.experience.com/er/security/login.jsp")
			}),
			new Group("Dining Services", new Child[] {
					new Child("Cafeteria Hours", "http://www.campusdish.com/en-US/CSMW/RoseHulman/Locations/HulmanUnionCafeteria.htm"),
					new Child("Cafeteria Menu", "http://www.campusdish.com/en-US/CSMW/RoseHulman/Locations/HulmanUnionCafeteriaMenu1.htm"),
					new Child("Subway Hours", "http://www.campusdish.com/en-US/CSMW/RoseHulman/Locations/Subway.htm"),
					new Child("Noble Roman's", "http://www.campusdish.com/en-US/CSMW/RoseHulman/Locations/NobleRomansintheWorx.htm"),
					new Child("C3", "http://www.campusdish.com/en-US/CSMW/RoseHulman/Locations/C3ConvenienceStore.htm"),
					new Child("Java City", "http://www.campusdish.com/en-US/CSMW/RoseHulman/Locations/JavaCity.htm"),
					new Child("Logan's", "http://www.campusdish.com/en-US/CSMW/RoseHulman/Locations/Logans.htm")
			}),
			new Group("Health Services", new Child[] {
					new Child("Hours and Staff", "http://www.rose-hulman.edu/HealthServices/staff.htm"),
					new Child("Services Offered", "http://www.rose-hulman.edu/HealthServices/services.htm"),
					new Child("Forms", "http://www.rose-hulman.edu/HealthServices/forms.htm")
			})
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.campus_services);
		
		tree = (ExpandableListView)findViewById(R.id.tree);
		tree.setAdapter(new TreeAdapter(groups));
		
		tree.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				tree_childClicked(groups[groupPosition].children[childPosition]);
				return true;
			}
		});
	}
	
	private void tree_childClicked(Child child) {
		String earl = child.url;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(earl));
		startActivity(intent);
	}
	
	private class Group {
		
		public String name;
		
		public Child[] children;
		
		public Group(String name, Child[] children) {
			this.name = name;
			this.children = children;
		}
	}
	
	private class Child {
		
		public String name;
		public String url;
		
		public Child(String name, String url) {
			this.name = name;
			this.url = url;
		}
		
	}
	
	private class TreeAdapter extends BaseExpandableListAdapter {
		
		public Group[] groups;
		
		public TreeAdapter(Group[] groups) {
			this.groups = groups;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			Group group = groups[groupPosition];
			
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.service_group, null);
			
			TextView name = (TextView)v.findViewById(R.id.name);
			
			name.setText(group.name);
			
			return v;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			Child child = groups[groupPosition].children[childPosition];
			
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
			return groups[groupPosition].children[childPosition];
		}

		@Override
		public int getGroupCount() {
			return groups.length;
		}
		
		@Override
		public int getChildrenCount(int groupPosition) {
			return groups[groupPosition].children.length;
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

}
