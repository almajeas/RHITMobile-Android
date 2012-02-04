package edu.rosehulman.android.directory;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ScheduleCourseActivity extends Activity {

	public static final String EXTRA_COURSE = "PERSON";
	public static final String EXTRA_SECTION = "SECTION";
	
	public static Intent createIntent(Context context, String course, int section) {
		Intent intent = new Intent(context, ScheduleCourseActivity.class);
		intent.putExtra(EXTRA_COURSE, course);
		intent.putExtra(EXTRA_SECTION, section);
		return intent;
	}
	
	private String course;
	private int section;
	
	private ListView detailsView;
	
	private ListItem listItems[];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_course);
        
        detailsView = (ListView)findViewById(R.id.details);
        
		Intent intent = getIntent();
		if (!(intent.hasExtra(EXTRA_COURSE) && intent.hasExtra(EXTRA_SECTION))) {
			finish();
			return;
		}
		course = intent.getStringExtra(EXTRA_COURSE);
		section = intent.getIntExtra(EXTRA_SECTION, 0);
        
        createListItems();
        
        setTitle(String.format("Course: %s-%02d", course, section));
        
        detailsView.setAdapter(new DetailsAdapter());
        detailsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				detailsView_itemClicked(position);
			}
		});
        
        
    }
    
    private void createListItems() {
    	List<ListItem> items = new LinkedList<ListItem>();
    	items.add(new ListHeader("General Info"));
    	items.add(new LabelItem("Name", "Senior Project"));
    	items.add(new InstructorItem("Shawn Bohner"));
    	items.add(new LabelItem("Enrollment", "24/25"));
    	
    	items.add(new ListHeader("Students"));
    	items.add(new PersonListItem("glowskst", "Scott Glowski", "Senior CS/SE/MA"));
    	items.add(new PersonListItem("theisje", "Jimmy Theis", "Senior SE"));
    	items.add(new PersonListItem("wattsbn", "Bryan Watts", "Senior CS/SE"));
    	items.add(new PersonListItem("wellska1", "Kevin Wells", "Senior SE/CS"));
    	
    	listItems = new ListItem[items.size()];
    	listItems = items.toArray(listItems);
    }
    
    private void detailsView_itemClicked(int position) {
    	listItems[position].onClick();
    }
    

    private interface ListItem {
    	public View getView();
    	public boolean isEnabled();
    	public void onClick();
    }
    
    private class ListHeader implements ListItem {
    	
    	private String name;
    	
    	public ListHeader(String name) {
    		this.name = name;
    	}

		@Override
		public View getView() {
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.list_section_header, null);
			
			TextView nameView = (TextView)v.findViewById(R.id.name);
			
			nameView.setText(name.toUpperCase());
			
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

    private class PersonListItem implements ListItem {
    	
    	public String username; 
		public String name;
    	public String description;
    	
    	public PersonListItem(String username, String name, String description) {
			this.username = username;
			this.name = name;
			this.description = description;
		}
    	
    	public View getView() {
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.list_item_two_line, null);
			
			TextView nameView = (TextView)v.findViewById(R.id.name);
			TextView descriptionView = (TextView)v.findViewById(R.id.description);
			
			nameView.setText(name);
			descriptionView.setText(description);
			
			return v;
    	}
    	
    	public boolean isEnabled() {
    		return true;
    	}
    	
    	public void onClick() {
    		Intent intent = PersonActivity.createIntent(ScheduleCourseActivity.this, username);
    		startActivity(intent);
    	}
    }
    
    private abstract class DetailsListItem implements ListItem {
    	
		public String name;
    	public String value;
    	
    	public DetailsListItem(String name, String value) {
			this.name = name;
			this.value = value;
		}
    	
    	public View getView() {
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.list_item_two_line, null);
			
			TextView nameView = (TextView)v.findViewById(R.id.name);
			TextView valueView = (TextView)v.findViewById(R.id.description);
			
			nameView.setText(name);
			valueView.setText(value);
			
			return v;
    	}
    	
    	public boolean isEnabled() {
    		return false;
    	}
    	
    	public abstract void onClick();
    }
    
    private abstract class ClickableListItem extends DetailsListItem {
    	
    	public ClickableListItem(String name, String value) {
    		super(name, value);
		}
    	
    	@Override
    	public boolean isEnabled() {
    		return true;
    	}
    }

    private class InstructorItem extends ClickableListItem {
    	
    	public InstructorItem(String value) {
    		super("Instructor", value);
		}
    	
    	@Override
    	public void onClick() {
    		Intent intent = PersonActivity.createIntent(ScheduleCourseActivity.this, this.value);
    		startActivity(intent);
    	}
    }
    
    private class LabelItem extends DetailsListItem {
    	
    	public LabelItem(String name, String value) {
    		super(name, value);
		}
    	
    	@Override
    	public void onClick() {
    		//do nothing
    	}
    }
    
    private class DetailsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listItems.length;
		}

		@Override
		public Object getItem(int position) {
			return listItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return listItems[position].getView();
		}
		
		@Override
		public boolean isEnabled(int position) {
			return listItems[position].isEnabled();
		}
		
    }
}
