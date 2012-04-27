package edu.rosehulman.android.directory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import edu.rosehulman.android.directory.model.Course;
import edu.rosehulman.android.directory.model.TermCode;
import edu.rosehulman.android.directory.service.AuthenticationException;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

public class ScheduleCourseActivity extends SherlockFragmentActivity {

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
	
	private TermCode term;
	private int crn;
	
	private ListView detailsView;
	
	private ListItem listItems[];
	
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
		
		getSupportFragmentManager().beginTransaction().add(new AuthenticatedFragment(), "auth").commit();
        
        createListItems();
        
        if (intent.hasExtra(EXTRA_COURSE)) {
        	setTitle(intent.getStringExtra(EXTRA_COURSE));
        }
        
        //TODO load course information
        
        detailsView.setAdapter(new DetailsAdapter());
        detailsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				detailsView_itemClicked(position);
			}
		});
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
    
    private void createListItems() {
    	List<ListItem> items = new LinkedList<ListItem>();
    	items.add(new ListHeader("General Info"));
    	items.add(new LabelItem("Name", "Senior Project"));
    	items.add(new LabelItem("Term", term.toString()));
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
    
    private class LoadCourse extends AsyncTask<Void, Void, Course> {
	
    	private TermCode term;
    	private int crn;
    	
    	private boolean serverError = false;
	
		public LoadCourse(TermCode term, int crn) {
			this.term = term;
			this.crn = crn;
		}

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}
		
		private boolean authenticate(MobileDirectoryService service) {
			return false;
			/*while (true) {
				try {
					//TODO authenticate
					return false;
				} catch (AuthenticationException e) {
					Log.e(C.TAG, "Invalid auth token", e);
					return false;
					
				} catch (ClientException e) {
					Log.e(C.TAG, "Client request failed", e);
					return false;
					
				} catch (ServerException e) {
					Log.e(C.TAG, "Server request failed", e);
					serverError = true;
					return false;
					
				} catch (JSONException e) {
					Log.e(C.TAG, "An error occured while parsing the JSON response", e);
					serverError = true;
					return false;
					
				} catch (IOException e) {
					Log.e(C.TAG, "Network error, retrying...", e);
				}
			}*/
		}
		
		private Course loadCourse(MobileDirectoryService service) {
			while (true) {
				try {
					return service.getCourse(User.getCookie(), term.code, crn);

				} catch (AuthenticationException e) {
					Log.e(C.TAG, "Invalid auth token", e);
					return null;
					
				} catch (ClientException e) {
					Log.e(C.TAG, "Client request failed", e);
					return null;
					
				} catch (ServerException e) {
					Log.e(C.TAG, "Server request failed", e);
					serverError = true;
					return null;
					
				} catch (JSONException e) {
					Log.e(C.TAG, "An error occured while parsing the JSON response", e);
					serverError = true;
					return null;
					
				} catch (IOException e) {
					Log.e(C.TAG, "Network error, retrying...", e);
				}
			}
		}

		@Override
		protected Course doInBackground(Void... params) {
			
			//get the person's course schedules
			MobileDirectoryService service = new MobileDirectoryService();
			Course course = loadCourse(service);
			
			//TODO convert the course
			return course;
		}
		
		@Override
		protected void onPostExecute(Course res) {
			setSupportProgressBarIndeterminateVisibility(false);
			//TODO load course info
		}
		
	}
}
