package edu.rosehulman.android.directory;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.AuthenticatedFragment.AuthenticationCallbacks;
import edu.rosehulman.android.directory.loaders.AsyncLoaderException;
import edu.rosehulman.android.directory.loaders.AsyncLoaderResult;
import edu.rosehulman.android.directory.loaders.InvalidAuthTokenException;
import edu.rosehulman.android.directory.loaders.LoadCourseInfo;
import edu.rosehulman.android.directory.model.Course;
import edu.rosehulman.android.directory.model.ShortUser;
import edu.rosehulman.android.directory.model.TermCode;

public class ScheduleCourseActivity extends SherlockFragmentActivity implements AuthenticationCallbacks {

	public static final String EXTRA_TERM = "Term";
	public static final String EXTRA_CRN = "CRN";
	public static final String EXTRA_COURSE = "Course";
	
	public static Intent createIntent(Context context, TermCode term, int crn, String course) {
		Intent intent = new Intent(context, ScheduleCourseActivity.class);
		intent.putExtra(EXTRA_TERM, term);
		intent.putExtra(EXTRA_CRN, crn);
		intent.putExtra(EXTRA_COURSE, course);
		return intent;
	}
	
    private static final int TASK_LOAD_COURSE = 1;
	
	private TermCode term;
	private int crn;
	
	private ListView detailsView;
	
	private ListItem listItems[];
	
	private AuthenticatedFragment mFragAuth;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_course);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        detailsView = (ListView)findViewById(R.id.details);
        
		Intent intent = getIntent();
		if (!(intent.hasExtra(EXTRA_TERM) && intent.hasExtra(EXTRA_CRN))) {
			finish();
			return;
		}
		term = intent.getParcelableExtra(EXTRA_TERM);
		crn = intent.getIntExtra(EXTRA_CRN, 0);
		
		FragmentManager fragments = getSupportFragmentManager();
        mFragAuth = (AuthenticatedFragment)fragments.findFragmentByTag("auth");
        if (mFragAuth == null) {
        	mFragAuth = new AuthenticatedFragment();
			getSupportFragmentManager().beginTransaction().add(mFragAuth, "auth").commit();
        }

        if (intent.hasExtra(EXTRA_COURSE)) {
        	setTitle(intent.getStringExtra(EXTRA_COURSE));
        }
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	mFragAuth.obtainAuthToken();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
			default:
				return super.onOptionsItemSelected(item); 
		}
		return true;
	}
    
    private void processResult(Course course) {
        createListItems(course);
        
        setTitle(course.title);
        getSupportActionBar().setSubtitle(course.course);
        
        detailsView.setAdapter(new DetailsAdapter());
        detailsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				detailsView_itemClicked(position);
			}
		});
    }
    
    private void createListItems(Course course) {
    	List<ListItem> items = new LinkedList<ListItem>();
    	items.add(new ListHeader("General Info"));
    	items.add(new LabelItem("Name", course.title));
    	items.add(new LabelItem("Credits", String.valueOf(course.credits)));
    	items.add(new LabelItem("Term", term.toString()));
    	items.add(new InstructorItem(course.instructor.fullname));
    	items.add(new LabelItem("Enrollment", String.format("%d/%d", course.enrolled, course.maxEnrollment)));
    	if (course.finalDay != '\0') {
    		String finalTime = "TBA";
    		switch (course.finalHour) {
    		case 1:
    			finalTime = "8am";
    			break;
    		case 2:
    			finalTime = "1pm";
    			break;
    		case 3:
    			finalTime = "6pm";
    			break;
    		}
    		String label = String.format("%c at %s in %s", 
    				Character.toUpperCase(course.finalDay),
    				finalTime,
    				course.finalRoom);
    		items.add(new LabelItem("Final", label));
    	}
    	
    	items.add(new ListHeader("Students"));
    	for (ShortUser student : course.students) {
    		items.add(new PersonListItem(student.username, student.fullname, student.subtitle));
    	}
    	
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

	@Override
	public void onAuthTokenObtained(String authToken) {
		loadCourse(authToken);
	}

	@Override
	public void onAuthTokenCancelled() {
		Toast.makeText(this, getString(R.string.authentication_error), Toast.LENGTH_SHORT).show();
		finish();
	}
    
    private Bundle mArgs;
	private void loadCourse(String authToken) {
		Bundle args = LoadCourseInfo.bundleArgs(authToken, term.code, crn);
		
		if (mArgs != null) {
			getSupportLoaderManager().restartLoader(TASK_LOAD_COURSE, args, mLoadScheduleCallbacks);
		} else {
			getSupportLoaderManager().initLoader(TASK_LOAD_COURSE, args, mLoadScheduleCallbacks);
		}
		
		mArgs = args;
	}
	
	private LoaderCallbacks<AsyncLoaderResult<Course>> mLoadScheduleCallbacks = new LoaderCallbacks<AsyncLoaderResult<Course>>() {

		@Override
		public Loader<AsyncLoaderResult<Course>> onCreateLoader(int id, Bundle args) {
			return new LoadCourseInfo(ScheduleCourseActivity.this, args);
		}

		@Override
		public void onLoadFinished(Loader<AsyncLoaderResult<Course>> loader, AsyncLoaderResult<Course> data) {
			Log.d(C.TAG, "Finished LoadUserSchedule");
			
			try {
				final Course result = data.getResult();
				setSupportProgressBarIndeterminateVisibility(false);
				processResult(result);

			} catch (InvalidAuthTokenException ex) {
				LoadCourseInfo scheduleLoader = (LoadCourseInfo)loader;
				mFragAuth.invalidateAuthToken(scheduleLoader.getAuthToken());
				mFragAuth.obtainAuthToken();
				
			} catch (AsyncLoaderException ex) {
				String message = ex.getMessage();
				if (message != null) {
					Toast.makeText(ScheduleCourseActivity.this, message, Toast.LENGTH_SHORT).show();
				}
				finish();
			}	
		}

		@Override
		public void onLoaderReset(Loader<AsyncLoaderResult<Course>> loader) {
		}
	};
}
