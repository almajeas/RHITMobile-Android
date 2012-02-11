package edu.rosehulman.android.directory;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PersonSearchActivity extends AuthenticatedListActivity {
	
	private String searchQuery;

	private PersonInfo[] people;
	private ArrayAdapter<PersonInfo> dataSet;
	
	private PersonInfo[] defaultPeople = new PersonInfo[] {
		new PersonInfo(1, "glowskst", "Scott Glowski", "CS/SE/MA Student"),
		new PersonInfo(2, "theisje", "Jimmy Theis", "SE Student"),
		new PersonInfo(3, "wellska1", "Kevin Wells", "SE/CS Student"),
		new PersonInfo(4, "wattsbn", "Bryan Watts", "CS/SE Student"),
		new PersonInfo(5, "hayesez", "Erik Hayes", "Assistant Dean of Student Affairs")
	};
	
	public class PersonInfo {
		public long id;
		public String username;
		public String name;
		public String description;
		
		public PersonInfo(long id, String username, String name, String description) {
			this.id = id;
			this.username = username;
			this.name = name;
			this.description = description;
		}	
	}
	
	private void runSearch(String query) {
		searchQuery = query;
		setTitle("Search: " + searchQuery);
		
		List<PersonInfo> res = new ArrayList<PersonInfo>();
		for (PersonInfo person : defaultPeople) {
			if (person.name.toLowerCase().contains(query.toLowerCase())) {
				res.add(person);
			}
		}
		people = new PersonInfo[res.size()];
		res.toArray(people);
		
		dataSet = new ArrayAdapter<PersonInfo>(PersonSearchActivity.this,
				R.layout.search_item, R.id.name, people) {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = inflater.inflate(R.layout.search_item, null);
				
				TextView name = (TextView)v.findViewById(R.id.name);
				TextView info = (TextView)v.findViewById(R.id.description);
				
				name.setText(people[position].name);
				info.setText(people[position].description);
				
				return v;
			}
		};
		setListAdapter(dataSet);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
    	if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
    		//are you lost?
    		return;
    	}
    	
		runSearch(intent.getStringExtra(SearchManager.QUERY));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_search);
		
    	Intent intent = getIntent();
    	
    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    		runSearch(intent.getStringExtra(SearchManager.QUERY));
    		
    	} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
		    Uri data = intent.getData();
		    
		    finish();
			Intent newIntent = PersonActivity.createIntent(this, data.getPath());
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(newIntent);
			
    	} else {
    		//are you lost?
    		finish();
    		return;
    	}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long rowId)
	{
	    finish();
		Intent newIntent = PersonActivity.createIntent(this, people[position].username);
		startActivity(newIntent);
	}

}
