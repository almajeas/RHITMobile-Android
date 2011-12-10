package edu.rosehulman.android.directory;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.rosehulman.android.directory.LoadLocation.OnLocationLoadedListener;
import edu.rosehulman.android.directory.model.Location;

public class PersonActivity extends Activity {

	public static final String EXTRA_PERSON = "PERSON"; 
	
	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, PersonActivity.class);
		intent.putExtra(EXTRA_PERSON, "");
		return intent;
	}
	
	private ImageView image;
	private TextView name;
	private ListView detailsView;
	
	private ListItem listItems[];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person);
        
        name = (TextView)findViewById(R.id.name);
        image = (ImageView)findViewById(R.id.image);
        detailsView = (ListView)findViewById(R.id.details);
        
        createListItems();
        
        name.setText("Kevin Wells");
        image.setImageResource(R.drawable.ic_contact_picture);
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
    	items.add(new ScheduleItem("Kevin Wells"));
    	items.add(new EmailItem("wellska1@rose-hulman.edu"));
    	items.add(new CallItem("1112223333,,1234"));
    	items.add(new LocationItem("F217"));
    	items.add(new LabelItem("Major", "SE/CS"));
    	items.add(new LabelItem("Campus Mailbox", "1965"));
    	
    	listItems = new ListItem[items.size()];
    	listItems = items.toArray(listItems);
    }
    
    private void detailsView_itemClicked(int position) {
    	listItems[position].onClick();
    }
    
    private abstract class ListItem {
    	
		public String name;
    	public String value;
    	public int icon;
    	
    	public ListItem(String name, String value, int icon) {
			this.name = name;
			this.value = value;
			this.icon = icon;
		}
    	
    	public View getView() {
			LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.icon_list_item, null);
			
			TextView nameView = (TextView)v.findViewById(R.id.name);
			TextView valueView = (TextView)v.findViewById(R.id.value);
			ImageView iconView = (ImageView)v.findViewById(R.id.icon);
			
			nameView.setText(name);
			valueView.setText(value);
			iconView.setImageResource(icon);
			
			return v;
    	}
    	
    	public boolean isEnabled() {
    		return false;
    	}
    	
    	public abstract void onClick();
    }
    
    private abstract class ClickableListItem extends ListItem {
    	
    	public ClickableListItem(String name, String value, int icon) {
    		super(name, value, icon);
		}
    	
    	@Override
    	public boolean isEnabled() {
    		return true;
    	}
    }
    
    private class ScheduleItem extends ClickableListItem {
    	
    	private String person;
    	
    	public ScheduleItem(String person) {
    		super("Schedule", null, android.R.drawable.sym_action_chat);
    		this.person = person;
		}
    	
    	@Override
    	public void onClick() {
    		Intent intent = PersonScheduleActivity.createIntent(PersonActivity.this, person);
    		startActivity(intent);
    	}
    }
    
    private class EmailItem extends ClickableListItem {
    	
    	public EmailItem(String value) {
    		super("Email", value, android.R.drawable.sym_action_email);
		}
    	
    	@Override
    	public void onClick() {
    		Intent intent = new Intent(Intent.ACTION_SENDTO);
    		intent.setData(Uri.fromParts("mailto", value, null));
    		startActivity(intent);
    	}
    }
    
    private class CallItem extends ClickableListItem {
    	
    	public CallItem(String value) {
    		super("Call", value, android.R.drawable.sym_action_call);
		}
    	
    	@Override
    	public void onClick() {
    		Intent intent = new Intent(Intent.ACTION_CALL);
    		intent.setData(Uri.fromParts("tel", value, null));
    		startActivity(intent);
    	}
    }
    
    private class LocationItem extends ClickableListItem {
    	
    	public LocationItem(String value) {
    		super("Room #", value, android.R.drawable.sym_action_chat);
		}
    	
    	@Override
    	public void onClick() {
    		new LoadLocation(new OnLocationLoadedListener() {
				@Override
				public void onLocationLoaded(Location location) {
					Intent intent = LocationActivity.createIntent(PersonActivity.this, location);
					startActivity(intent);
				}
			}).execute((long)1362170);
    		//FIXME find the actual location ID
    	}
    }
    
    private class LabelItem extends ListItem {
    	
    	public LabelItem(String name, String value) {
    		super(name, value, android.R.id.empty);
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
