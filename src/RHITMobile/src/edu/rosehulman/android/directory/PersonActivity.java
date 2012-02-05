package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.rosehulman.android.directory.LoadLocation.OnLocationLoadedListener;
import edu.rosehulman.android.directory.model.Location;

public class PersonActivity extends AuthenticatedActivity {

	public static final String EXTRA_PERSON = "PERSON"; 
	
	public static Intent createIntent(Context context) {
		return createIntent(context, "");
	}

	public static Intent createIntent(Context context, String person) {
		Intent intent = new Intent(context, PersonActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		return intent;
	}
	
	private ImageView image;
	private TextView name;
	private ListView detailsView;
	
	private ListItem listItems[];
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
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
        case R.id.add_contact:
        	//TODO finish implementing createContact
        	//createContact();
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
	private void createContact() {
		ArrayList<ContentProviderOperation> ops =
				new ArrayList<ContentProviderOperation>();
		int rawContactInsertIndex = ops.size();
		ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_TYPE, "RHIT")
				.withValue(RawContacts.ACCOUNT_NAME, "RHIT")
				.build());

		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
				.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
				.withValue(StructuredName.DISPLAY_NAME, "Eric Hayes")
				.build());

		try {
			getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			e.printStackTrace();
			return;
		} catch (OperationApplicationException e) {
			e.printStackTrace();
			return;
		}

	}
    
    private void createListItems() {
    	List<ListItem> items = new LinkedList<ListItem>();
    	items.add(new ScheduleItem("Kevin Wells"));
    	items.add(new EmailItem("wellska1@rose-hulman.edu"));
    	items.add(new CallItem("1112223333 x1234"));
    	items.add(new LocationItem("F217"));
    	items.add(new LabelItem("Major", "SE/CS"));
    	items.add(new LabelItem("Class", "Senior"));
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
    		Intent intent = SchedulePersonActivity.createIntent(PersonActivity.this, person);
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
