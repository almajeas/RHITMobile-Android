package edu.rosehulman.android.directory;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PersonSearchActivity extends ListActivity {
	
	private String searchQuery;

	private PersonInfo[] people;
	private ArrayAdapter<PersonInfo> dataSet;
	
	private PersonInfo[] defaultPeople = new PersonInfo[] {
		new PersonInfo(1, "Kevin Wells", "SE/CS Student"),
		new PersonInfo(2, "Eric Hayes", "Assistant Dean of Student Affairs"),
		new PersonInfo(3, "Matt Branam", "President")
	};
	
	public class PersonInfo {
		public long id;
		public String name;
		public String description;
		
		public PersonInfo(long id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}	
	}
	
	private void runSearch(String query) {
		searchQuery = query;
		setTitle("Search: " + searchQuery);
		
		//TODO update
		people = defaultPeople;
		
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
		    //Uri data = intent.getData();
		    
		    //long id = Long.parseLong(data.getPath());
		    finish();
			Intent newIntent = PersonActivity.createIntent(this);
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
		//long id = locations[position].id; 
	    
	    finish();
		Intent newIntent = PersonActivity.createIntent(this);
		newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(newIntent);
	}

}
