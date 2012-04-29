package edu.rosehulman.android.directory;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
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
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.AuthenticatedFragment.AuthenticationCallbacks;
import edu.rosehulman.android.directory.LoadLocation.OnLocationLoadedListener;
import edu.rosehulman.android.directory.loaders.AsyncLoaderException;
import edu.rosehulman.android.directory.loaders.AsyncLoaderResult;
import edu.rosehulman.android.directory.loaders.InvalidAuthTokenException;
import edu.rosehulman.android.directory.loaders.LoadUser;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.model.UserDataResponse;

public class PersonActivity extends SherlockFragmentActivity implements AuthenticationCallbacks {

	public static final String EXTRA_USERNAME = "Username"; 
	
	public static Intent createIntent(Context context) {
		return createIntent(context, "");
	}

	public static Intent createIntent(Context context, String username) {
		Intent intent = new Intent(context, PersonActivity.class);
		intent.putExtra(EXTRA_USERNAME, username);
		return intent;
	}
	
	private static final int TASK_LOAD_USER = 1;
	
	private String mUsername;
	
	private ListView detailsView;
	
	private ListItem listItems[];
	
	private AuthenticatedFragment mFragAuth;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        detailsView = (ListView)findViewById(R.id.details);
        
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(EXTRA_USERNAME);
        Log.d(C.TAG, "Person: " + mUsername);
        
		FragmentManager fragments = getSupportFragmentManager();
        mFragAuth = (AuthenticatedFragment)fragments.findFragmentByTag("auth");
        if (mFragAuth == null) {
        	mFragAuth = new AuthenticatedFragment();
			getSupportFragmentManager().beginTransaction().add(mFragAuth, "auth").commit();
        }
        
        LoaderManager loaders = getSupportLoaderManager();
		if (loaders.getLoader(TASK_LOAD_USER) != null) {
			loaders.initLoader(TASK_LOAD_USER, null, mLoadScheduleCallbacks);
		}
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	mFragAuth.obtainAuthToken();
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
    
    private void processResult(UserDataResponse user) {
        updateUI(user);
        
        detailsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				detailsView_itemClicked(position);
			}
		});
    }
    
    private void updateUI(UserDataResponse user) {
    	setTitle(String.format("%s %s", user.firstName, user.lastName));
    	
    	List<ListItem> items = new LinkedList<ListItem>();
    	items.add(new LabelItem("Full Name", user.getFullName()));
    	items.add(new ScheduleItem(user.user.username));
    	items.add(new EmailItem(user.email));
    	if (!TextUtils.isEmpty(user.telephone))
    		items.add(new CallItem(user.telephone));
    	if (!TextUtils.isEmpty(user.office))
    		items.add(new LocationItem(user.office));
    	if (!TextUtils.isEmpty(user.department))
    		items.add(new LabelItem("Department", user.department));
    	if (!TextUtils.isEmpty(user.majors.trim()))
    		items.add(new LabelItem("Majors", user.majors));
    	if (!TextUtils.isEmpty(user.currentClass.trim()));
    		items.add(new LabelItem("Class", user.currentClass));
    	if (user.cm > 0)
    		items.add(new LabelItem("Campus Mailbox", String.valueOf(user.cm)));

    	listItems = new ListItem[items.size()];
    	listItems = items.toArray(listItems);
    	detailsView.setAdapter(new DetailsAdapter());
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
			if (icon != 0) {
				iconView.setImageResource(icon);
			}
			
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
    		super(name, value, 0);
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
    
	@Override
	public void onAuthTokenObtained(String authToken) {
		loadUser(authToken);
	}

	@Override
	public void onAuthTokenCancelled() {
		Toast.makeText(this, getString(R.string.authentication_error), Toast.LENGTH_SHORT).show();
		finish();
	}
    
    private Bundle mArgs;
	private void loadUser(String authToken) {
		Bundle args = LoadUser.bundleArgs(authToken, mUsername);
		
		if (mArgs != null) {
			getSupportLoaderManager().restartLoader(TASK_LOAD_USER, args, mLoadScheduleCallbacks);
		} else {
			getSupportLoaderManager().initLoader(TASK_LOAD_USER, args, mLoadScheduleCallbacks);
		}
		
		mArgs = args;
	}
	
	private LoaderCallbacks<AsyncLoaderResult<UserDataResponse>> mLoadScheduleCallbacks = new LoaderCallbacks<AsyncLoaderResult<UserDataResponse>>() {

		@Override
		public Loader<AsyncLoaderResult<UserDataResponse>> onCreateLoader(int id, Bundle args) {
			return new LoadUser(PersonActivity.this, args);
		}

		@Override
		public void onLoadFinished(Loader<AsyncLoaderResult<UserDataResponse>> loader, AsyncLoaderResult<UserDataResponse> data) {
			Log.d(C.TAG, "Finished LoadUserSchedule");
			
			try {
				final UserDataResponse result = data.getResult();
				setSupportProgressBarIndeterminateVisibility(false);
				processResult(result);

			} catch (InvalidAuthTokenException ex) {
				LoadUser load = (LoadUser)loader;
				mFragAuth.invalidateAuthToken(load.getAuthToken());
				mFragAuth.obtainAuthToken();
				
			} catch (AsyncLoaderException ex) {
				String message = ex.getMessage();
				if (message != null) {
					Toast.makeText(PersonActivity.this, message, Toast.LENGTH_SHORT).show();
				}
				finish();
			}	
		}

		@Override
		public void onLoaderReset(Loader<AsyncLoaderResult<UserDataResponse>> loader) {
		}
	};
}
