package edu.rosehulman.android.directory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.LoadLocation.OnLocationLoadedListener;
import edu.rosehulman.android.directory.model.Location;

public class PersonActivity extends SherlockFragmentActivity {

	public static final String EXTRA_PERSON = "PERSON"; 
	
	public static Intent createIntent(Context context) {
		return createIntent(context, "");
	}

	public static Intent createIntent(Context context, String person) {
		Intent intent = new Intent(context, PersonActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		return intent;
	}
	
	private ListView detailsView;
	
	private ListItem listItems[];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        detailsView = (ListView)findViewById(R.id.details);
        
        Intent intent = getIntent();
        String username = intent.getStringExtra(EXTRA_PERSON);
        Log.d(C.TAG, "Person: " + username);
        PersonInfo person;
        if (personMap.containsKey(username)) {
        	person = personMap.get(username);
        } else {
        	person = defaultPerson;
        }
        
        getSupportFragmentManager().beginTransaction().add(new AuthenticatedFragment(), "auth").commit();
        
        updateUI(person);
        
        detailsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				detailsView_itemClicked(position);
			}
		});

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.person, menu);
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
        case android.R.id.home:
        	finish();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void updateUI(PersonInfo person) {
    	setTitle(person.name);
    	
    	List<ListItem> items = new LinkedList<ListItem>();
    	items.add(new ScheduleItem(person.username));
    	items.add(new EmailItem("wellska1@rose-hulman.edu"));
    	items.add(new CallItem("1112223333 x1234"));
    	items.add(new LocationItem("F217"));
    	if (person.major != null)
    		items.add(new LabelItem("Major", person.major));
    	if (person.year != null)
    		items.add(new LabelItem("Class", person.year));
    	items.add(new LabelItem("Campus Mailbox", "1965"));
    	
    	listItems = new ListItem[items.size()];
    	listItems = items.toArray(listItems);
    	detailsView.setAdapter(new DetailsAdapter());
    }
    
    private void detailsView_itemClicked(int position) {
    	listItems[position].onClick();
    }
    
	private PersonInfo defaultPerson = new PersonInfo(3, "wellska1", "Kevin Wells", "SE/CS", "Senior");
	@SuppressWarnings("serial")
	private Map<String, PersonInfo> personMap = new HashMap<String, PersonInfo>() {
		PersonInfo[] defaultPeople = new PersonInfo[] {
				new PersonInfo(1, "glowskst", "Scott Glowski", "CS/SE/MA", "Senior"),
				new PersonInfo(2, "theisje", "Jimmy Theis", "SE", "Senior"),
				defaultPerson,
				new PersonInfo(4, "wattsbn", "Bryan Watts", "CS/SE", "Senior"),
				new PersonInfo(5, "hayesez", "Erik Hayes", null, null)};
		{
			for (PersonInfo person : defaultPeople) {
				put(person.username, person);
			}
		}
	};

	public class PersonInfo {
		public long id;
		public String username;
		public String name;
		public String major;
		public String year;

		public PersonInfo(long id, String username, String name, String major, String year) {
			this.id = id;
			this.username = username;
			this.name = name;
			this.major = major;
			this.year = year;
		}
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
    		super("Schedule", null, R.drawable.action_schedule);
    		this.person = person;
		}
    	
    	@Override
    	public void onClick() {
    		Intent intent = SchedulePersonActivity.createIntent(PersonActivity.this, person);
    		startActivity(intent);
    	}
    }
    
    private class EmailItem extends ClickableListItem {
    	
    	public EmailItem(String value) {
    		super("Email", value, R.drawable.action_email);
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
    		super("Call", value, R.drawable.action_call);
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
    		super("Room #", value, R.drawable.action_location);
		}
    	
    	@Override
    	public void onClick() {
    		new LoadLocation((long)1362170, new OnLocationLoadedListener() {
				@Override
				public void onLocationLoaded(Location location) {
					Intent intent = LocationActivity.createIntent(PersonActivity.this, location);
					startActivity(intent);
				}
			}).execute();
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
