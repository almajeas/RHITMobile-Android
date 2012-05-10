package edu.rosehulman.android.directory;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListItems {

    public interface ListItem {
    	public View getView();
    	public boolean isEnabled();
    	public void onClick();
    }
    
    public static class ListHeader implements ListItem {
    	
    	private String mName;
    	private Context mContext;
    	
    	public ListHeader(Context context, String name) {
    		mContext = context;
    		mName = name;
    	}

		@Override
		public View getView() {
			View v = LayoutInflater.from(mContext).inflate(R.layout.list_section_header, null);
			
			TextView nameView = (TextView)v.findViewById(R.id.name);
			
			nameView.setText(mName.toUpperCase());
			
			return v;
		}

		@Override
		public boolean isEnabled() {
			return false;
		}

		@Override
		public void onClick() {
			
		}
    }
    
    public static abstract class DetailsListItem implements ListItem {
    	
    	private Context mContext;
		private String mName;
    	private String mValue;
    	
    	public DetailsListItem(Context context, String name, String value) {
    		mContext = context;
			mName = name;
			mValue = value;
		}
    	
    	public View getView() {
    		View v;
    		
    		if (mValue == null) {
    			v = LayoutInflater.from(mContext).inflate(R.layout.list_item_one_line, null);
    		} else {
    			v = LayoutInflater.from(mContext).inflate(R.layout.list_item_two_line, null);	
    		}
			
			TextView nameView = (TextView)v.findViewById(R.id.name);
			nameView.setText(mName);
			
			if (mValue != null) {
				TextView valueView = (TextView)v.findViewById(R.id.description);
				valueView.setText(mValue);
			} else {
				v.findViewById(R.id.divider).setVisibility(View.VISIBLE);
			}
			
			return v;
    	}
    	
    	public boolean isEnabled() {
    		return false;
    	}
    	
    	public abstract void onClick();
    }
    
    public static abstract class ClickableListItem extends DetailsListItem {
    	
    	public ClickableListItem(Context context, String name, String value) {
    		super(context, name, value);
		}
    	
    	@Override
    	public boolean isEnabled() {
    		return true;
    	}
    }
    
    public static class DetailsAdapter extends BaseAdapter {
    	
    	private List<ListItem> mDetails;
    	
    	public DetailsAdapter(List<ListItem> details) {
    		mDetails = details;
    	}

		@Override
		public int getCount() {
			return mDetails.size();
		}

		@Override
		public ListItem getItem(int position) {
			return mDetails.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return mDetails.get(position).getView();
		}
		
		@Override
		public boolean isEnabled(int position) {
			return mDetails.get(position).isEnabled();
		}
    }
    
}
