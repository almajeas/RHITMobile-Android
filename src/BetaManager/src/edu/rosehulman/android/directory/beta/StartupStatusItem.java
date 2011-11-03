package edu.rosehulman.android.directory.beta;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StartupStatusItem extends LinearLayout {
	
	public enum StatusState {
		IN_PROGRESS,
		ACTION_REQUIRED,
		ERROR,
		SUCCESS
	}

	private TextView message;
	
	private ImageView image;
	private View spinner;
	
	public StartupStatusItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.startup_status_item, this);

		message = (TextView)findViewById(R.id.message);
		image = (ImageView)findViewById(R.id.image);
		spinner = findViewById(R.id.spinner);
	}
	
	public void setMessage(String text) {
		message.setText(text);
	}
	
	public void setState(StatusState state) {
		switch (state) {
		case IN_PROGRESS:
			spinner.setVisibility(VISIBLE);
			image.setVisibility(INVISIBLE);
			break;
		case ACTION_REQUIRED:
			spinner.setVisibility(INVISIBLE);
			image.setVisibility(VISIBLE);
			image.setImageResource(R.drawable.warning);
			break;
		case ERROR:
			spinner.setVisibility(INVISIBLE);
			image.setVisibility(VISIBLE);
			image.setImageResource(R.drawable.error);
			break;
		case SUCCESS:
			spinner.setVisibility(INVISIBLE);
			image.setVisibility(VISIBLE);
			image.setImageResource(R.drawable.success);
			break;
				
		}
	}

}
