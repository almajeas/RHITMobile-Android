package edu.rosehulman.android.directory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class PersonScheduleActivity extends Activity {
	
	public static final String EXTRA_PERSON = "PERSON";
	
	public static Intent createIntent(Context context, String person) {
		Intent intent = new Intent(context, PersonScheduleActivity.class);
		intent.putExtra(EXTRA_PERSON, person);
		return intent;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.person_schedule);

		//TODO don't just redirect to a webpage
		String earl = "https://prodweb.rose-hulman.edu/regweb-cgi/reg-sched.pl?termcode=201220&view=tgrid&id1=wellska1&bt1=ID%2Username";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(earl));
		startActivity(intent);
		finish();
	}

}
