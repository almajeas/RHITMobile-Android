package edu.rosehulman.android.directory;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class PersonListFragment extends SherlockListFragment {
	
	private String searchQuery;

	private PersonInfo[] people;
	private ArrayAdapter<PersonInfo> dataSet;

	private PersonInfo[] defaultPeople = new PersonInfo[] {
			new PersonInfo("glowskst", "Scott Glowski", "CS/SE/MA Student"),
			new PersonInfo("theisje", "Jimmy Theis", "SE Student"),
			new PersonInfo("wellska1", "Kevin Wells", "SE/CS Student"),
			new PersonInfo("wattsbn", "Bryan Watts", "CS/SE Student"),
			new PersonInfo("hayesez", "Erik Hayes", "Assistant Dean of Student Affairs")
	};

	public class PersonInfo {
		public String username;
		public String name;
		public String description;

		public PersonInfo(String username, String name, String description) {
			this.username = username;
			this.name = name;
			this.description = description;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		Activity activity = getActivity();

		setEmptyText(activity.getString(R.string.no_results));

		Intent intent = activity.getIntent();

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			runSearch(intent.getStringExtra(SearchManager.QUERY));

		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri data = intent.getData();

			activity.finish();
			Intent newIntent = PersonActivity.createIntent(getActivity(), data.getPath());
			newIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(newIntent);

		} else {
			//are you lost?
			activity.finish();
			return;
		}
	}

	public void runSearch(String query) {
		searchQuery = query;
		getSherlockActivity().getSupportActionBar().setSubtitle(searchQuery);

		List<PersonInfo> res = new ArrayList<PersonInfo>();
		Log.d(C.TAG, "Query: " + query);
		for (PersonInfo person : defaultPeople) {
			if (person.name.toLowerCase().contains(query.toLowerCase())) {
				Log.d(C.TAG, "Checked person: " + person.username);
				res.add(person);
			}
		}
		people = new PersonInfo[res.size()];
		res.toArray(people);

		dataSet = new ArrayAdapter<PersonInfo>(getActivity(),
				R.layout.search_item, R.id.name, people) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
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
	public void onListItemClick(ListView l, View v, int position, long rowId)
	{
		getActivity().finish();
		Intent newIntent = PersonActivity.createIntent(getActivity(), people[position].username);
		startActivity(newIntent);
	}
	
}
