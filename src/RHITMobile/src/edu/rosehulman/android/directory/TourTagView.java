package edu.rosehulman.android.directory;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import edu.rosehulman.android.directory.model.TourTag;
import edu.rosehulman.android.directory.model.TourTagsGroup;

public class TourTagView extends LinearLayout {
	
	private ExpandableListView groups;
	private ListView tags;
	
	private LayoutInflater inflater;
	
	public TourTagView(Context context) {
		this(context, null);
	}
	
	public TourTagView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.tour_tag_list, this, true);
		
		groups = (ExpandableListView)findViewById(R.id.groups);
		tags = (ListView)findViewById(R.id.tags);
		
		this.setPadding(getPaddingLeft()+15, getPaddingTop(), getPaddingRight(), getPaddingBottom());
	}
	
	public void setData(final TourTagsGroup root) {
		groups.setAdapter(new TagGroupsAdapter(root.children));
		tags.setAdapter(new TagsAdapter(root.tags));
		
		groups.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				Log.d(C.TAG, "Group collapsed: " + root.children[groupPosition].name);
			}
		});
		
		groups.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				Log.d(C.TAG, "Group expanded: " + root.children[groupPosition].name);
			}
		});
		
	}
	
	private class TagsAdapter extends BaseAdapter {
		
		private TourTag[] tags;
		
		public TagsAdapter(TourTag[] tags) {
			this.tags = tags;
		}

		@Override
		public int getCount() {
			return tags.length;
		}

		@Override
		public Object getItem(int index) {
			return tags[index];
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int index, View convertView, ViewGroup parent) {
			TourTag tag = tags[index];
			
			View v = inflater.inflate(R.layout.tour_tag_child, null);
			
			TextView name = (TextView)v.findViewById(R.id.name);
			
			name.setText(tag.name);
			
			return v;
		}
	}
	
	private class TagGroupsAdapter extends BaseExpandableListAdapter {
		
		public TourTagsGroup[] groups;
		
		public TagGroupsAdapter(TourTagsGroup[] groups) {
			this.groups = groups;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			TourTagsGroup group = groups[groupPosition];
			
			Log.d(C.TAG, "Make group view: " + group.name + " ;expanded: " + isExpanded);
			
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.tour_tag_group, null);
			}
			TextView name = (TextView)v.findViewById(R.id.name);
			
			name.setText(group.name);
			
			return v;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			TourTagsGroup child = groups[groupPosition];
			
			TourTagView res = (TourTagView)convertView;
			if (res == null) {
				res = new TourTagView(TourTagView.this.groups.getContext());
				res.setData(child);
			}
			
			return res;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groups[groupPosition];
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return groups[groupPosition];
		}

		@Override
		public int getGroupCount() {
			return groups.length;
		}
		
		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groups[groupPosition].name.hashCode();
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groups[groupPosition].name.hashCode()-1;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
		
	}

}
