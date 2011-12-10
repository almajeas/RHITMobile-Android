package edu.rosehulman.android.directory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class StartupActivity extends Activity {
	
	private ArrayAdapter<Task> taskAdapter;
	
	private GridView tasksView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		
		taskAdapter = new ArrayAdapter<Task>(
				this, 
				R.layout.startup_task, 
				R.id.task_label, 
				tasks) {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = inflater.inflate(R.layout.startup_task, null);
				
				TextView name = (TextView)v.findViewById(R.id.task_label);
				ImageView icon = (ImageView)v.findViewById(R.id.task_image);
				
				name.setText(tasks[position].name);
				icon.setImageResource(tasks[position].image);
				
				return v;
			}
		};
		
		tasksView = (GridView)findViewById(R.id.tasks);
		tasksView.setAdapter(taskAdapter);
		tasksView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				tasks[position].listener.onClick(v);
			}
		});
		
		
	}
	
	private void taskMap_clicked() {
		Intent intent = CampusMapActivity.createIntent(this);
		startActivity(intent);
	}
	
	private void taskDirectory_clicked() {
		
	}
	
	private void taskServices_clicked() {
		
	}
	
	private class Task {
		public String name;
		public OnClickListener listener;
		public int image;
		
		public Task(String name, int image, OnClickListener listener) {
			this.name = name;
			this.image = image;
			this.listener = listener;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private Task[] tasks = new Task[] {
			new Task("Campus Map",
					android.R.drawable.ic_menu_mapmode,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					taskMap_clicked();
				}
			}), 
			new Task("Directory",
					android.R.drawable.ic_menu_send,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					taskDirectory_clicked();
				}
			}), 
			new Task("Campus Services",
					android.R.drawable.ic_menu_slideshow,
					new OnClickListener() {
				@Override
				public void onClick(View v) {
					taskServices_clicked();
				}
			})
		};
	

}
