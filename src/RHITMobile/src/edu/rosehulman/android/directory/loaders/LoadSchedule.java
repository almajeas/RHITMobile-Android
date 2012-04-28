package edu.rosehulman.android.directory.loaders;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.model.Course;
import edu.rosehulman.android.directory.model.CourseMeeting;
import edu.rosehulman.android.directory.model.CoursesResponse;
import edu.rosehulman.android.directory.model.PersonScheduleDay;
import edu.rosehulman.android.directory.model.PersonScheduleItem;
import edu.rosehulman.android.directory.model.PersonScheduleWeek;
import edu.rosehulman.android.directory.service.AuthenticationException;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

public class LoadSchedule extends CachedAsyncLoader<PersonScheduleWeek> {
	
	private static final String ARG_AUTH_TOKEN = "AuthToken";
	private static final String ARG_USERNAME = "Username";
	
	public static Bundle bundleArgs(String authToken, String username) {
		Bundle res = new Bundle();
		res.putString(ARG_AUTH_TOKEN, authToken);
		res.putString(ARG_USERNAME, username);
		return res;
	}

	private String mAuthToken;
	private String mUsername;
	
	public LoadSchedule(Context context, Bundle args) {
		super(context);
		mAuthToken = args.getString(ARG_AUTH_TOKEN);
		mUsername = args.getString(ARG_USERNAME);
	}

	public String getAuthToken() {
		return mAuthToken;
	}
	
	@Override
	protected PersonScheduleWeek doInBackground() throws AsyncLoaderException {
		Log.d(C.TAG, "Starting schedule loader");

		//get the person's course schedules
		MobileDirectoryService service = new MobileDirectoryService();
		CoursesResponse response = loadCourses(service);
		
		if (response == null) {
			return null;
		}

		//convert the course schedules to a user schedule
		PersonScheduleWeek schedule = new PersonScheduleWeek();
		
		for (Course course : response.courses) {
			for (CourseMeeting meeting : course.schedule) {
				PersonScheduleDay day = schedule.getDay(meeting.day);
				PersonScheduleItem item;
				item = new PersonScheduleItem(course.crn, course.course, course.title, meeting.startPeriod, meeting.endPeriod, meeting.room);
				day.addItem(item);
			}
		}
		
		return schedule;
	}
	
	private CoursesResponse loadCourses(MobileDirectoryService service) throws AsyncLoaderException {
		while (true) {
			if (Thread.interrupted())
				return null;
			
			try {
				Log.d(C.TAG, "Retrieving schedule");
				return service.getUserSchedule(mAuthToken, mUsername);

			} catch (AuthenticationException e) {
				Log.w(C.TAG, "Invalid auth token");
				throw new InvalidAuthTokenException();
				
			} catch (ClientException e) {
				Log.e(C.TAG, "Client request failed", e);
				throw new AsyncLoaderException("Invalid request");
				
			} catch (ServerException e) {
				Log.e(C.TAG, "Server request failed", e);
				throw new AsyncLoaderException("Service is rejecting requests. Please try again later.");
				
			} catch (JSONException e) {
				Log.e(C.TAG, "An error occured while parsing the JSON response", e);
				throw new AsyncLoaderException("Service is rejecting requests. Please try again later.");
				
			} catch (IOException e) {
				Log.e(C.TAG, "Network error, retrying...", e);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					return null;
				}
			}
		}
	}
}
