package edu.rosehulman.android.directory;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import edu.rosehulman.android.directory.model.Location;
import edu.rosehulman.android.directory.tasks.LoadLocation;
import edu.rosehulman.android.directory.tasks.LoadLocation.OnLocationLoadedListener;

public class ClickableLocationSpan extends ClickableSpan {
	
	private Context context;
	private String name;

	public ClickableLocationSpan(Context context, String name) {
		this.context = context;
		this.name = name;
	}

	@Override
	public void onClick(View v) {
		final CharSequence[] options = {"Room Info", "Room Schedule"};

    	new AlertDialog.Builder(context)
    		.setTitle("Select action")
    		.setItems(options, new DialogInterface.OnClickListener() {
	    	    public void onClick(DialogInterface dialog, int item) {
	    	    	switch (item) {
	    	    	case 0: //room info
	    	    		new LoadLocation(name, new OnLocationLoadedListener() {
	    	    			@Override
	    	    			public void onLocationLoaded(Location location) {
	    	    				if (location == null) {
	    	    					Toast.makeText(context, "Location info not found", Toast.LENGTH_SHORT).show();
	    	    					return;
	    	    				}
	    	    				Intent intent = LocationActivity.createIntent(context, location);
	    	    				context.startActivity(intent);
	    	    			}
	    	    		}).execute();
	    	    		break;
	    	    		
	    	    	case 1: //room schedule
	    	    		Intent intent = ScheduleRoomActivity.createIntent(context, name);
	    	    		context.startActivity(intent);
	    	    		break;
	    	    	}
	    	    }
	    	})
	    	.show();
	}
	
	public static void linkify(TextView v, String text) {
		SpannableString s = new SpannableString(text);
		s.setSpan(new ClickableLocationSpan(v.getContext(), text), 0, text.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
		v.setText(s);
	}
}
