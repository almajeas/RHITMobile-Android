package edu.rosehulman.android.directory;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import edu.rosehulman.android.directory.fragments.AuthenticatedFragment;
import edu.rosehulman.android.directory.fragments.AuthenticatedFragment.AuthenticationCallbacks;
import edu.rosehulman.android.directory.loaders.InvalidAuthTokenException;
import edu.rosehulman.android.directory.loaders.LoadCourseInfo;
import edu.rosehulman.android.directory.loaders.LoaderException;
import edu.rosehulman.android.directory.loaders.LoaderResult;
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
	
	private AuthenticatedFragment mFragAuth;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.schedule_course);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        setSupportProgressBarIndeterminateVisibility(true);
        
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
        
        LoaderManager loaders = getSupportLoaderManager();
		if (loaders.getLoader(TASK_LOAD_COURSE) != null) {
			loaders.initLoader(TASK_LOAD_COURSE, null, mLoadCourseCallbacks);
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
        final ListItems.DetailsAdapter adapter = createDetailsAdapter(course);
        
        setTitle(course.title);
        getSupportActionBar().setSubtitle(course.course);
        
        detailsView.setAdapter(adapter);
        detailsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				detailsView_itemClicked(adapter.getItem(position));
			}
		});
    }
    
    private ListItems.DetailsAdapter createDetailsAdapter(Course course) {
    	List<ListItems.ListItem> items = new LinkedList<ListItems.ListItem>();
    	items.add(new ListItems.ListHeader(this, "General Info"));
    	items.add(new LabelItem("Name", course.title));
    	items.add(new LabelItem("Credits", String.valueOf(course.credits)));
    	items.add(new LabelItem("Term", term.toString()));
    	items.add(new InstructorItem(course.instructor));
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
    	
    	items.add(new ListItems.ListHeader(this, "Students"));
    	for (ShortUser student : course.students) {
    		items.add(new PersonListItem(student.username, student.fullname, student.subtitle));
    	}
    	
    	return new ListItems.DetailsAdapter(items);
    }
    
    private void detailsView_itemClicked(ListItems.ListItem item) {
    	item.onClick();
    }

    private class PersonListItem implements ListItems.ListItem {
    	
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

    private class InstructorItem extends ListItems.ClickableListItem {
    	
    	private ShortUser mUser;
    	
    	public InstructorItem(ShortUser user) {
    		super(ScheduleCourseActivity.this, "Instructor", user.fullname);
    		mUser = user;
		}
    	
    	@Override
    	public void onClick() {
    		Intent intent = PersonActivity.createIntent(ScheduleCourseActivity.this, mUser.username);
    		startActivity(intent);
    	}
    }
    
    private class LabelItem extends ListItems.DetailsListItem {
    	
    	public LabelItem(String name, String value) {
    		super(ScheduleCourseActivity.this, name, value);
		}
    	
    	@Override
    	public void onClick() {
    		//do nothing
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
			getSupportLoaderManager().restartLoader(TASK_LOAD_COURSE, args, mLoadCourseCallbacks);
		} else {
			getSupportLoaderManager().initLoader(TASK_LOAD_COURSE, args, mLoadCourseCallbacks);
		}
		
		mArgs = args;
	}
	
	private LoaderCallbacks<LoaderResult<Course>> mLoadCourseCallbacks = new LoaderCallbacks<LoaderResult<Course>>() {

		@Override
		public Loader<LoaderResult<Course>> onCreateLoader(int id, Bundle args) {
			return new LoadCourseInfo(ScheduleCourseActivity.this, args);
		}

		@Override
		public void onLoadFinished(Loader<LoaderResult<Course>> loader, LoaderResult<Course> data) {
			Log.d(C.TAG, "Finished LoadCourseInfo");
			
			try {
				final Course result = data.getResult();
				setSupportProgressBarIndeterminateVisibility(false);
				processResult(result);

			} catch (InvalidAuthTokenException ex) {
				LoadCourseInfo scheduleLoader = (LoadCourseInfo)loader;
				mFragAuth.invalidateAuthToken(scheduleLoader.getAuthToken());
				mFragAuth.obtainAuthToken();
				
			} catch (LoaderException ex) {
				String message = ex.getMessage();
				if (message != null) {
					Toast.makeText(ScheduleCourseActivity.this, message, Toast.LENGTH_SHORT).show();
				}
				finish();
			}	
		}

		@Override
		public void onLoaderReset(Loader<LoaderResult<Course>> loader) {
		}
	};
}
