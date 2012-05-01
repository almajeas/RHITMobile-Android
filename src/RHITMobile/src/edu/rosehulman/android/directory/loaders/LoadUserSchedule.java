package edu.rosehulman.android.directory.loaders;

import java.io.IOException;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import edu.rosehulman.android.directory.C;
import edu.rosehulman.android.directory.R;
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

public class LoadUserSchedule extends CachedAsyncLoader<PersonScheduleWeek> {
	
	private static final String ARG_AUTH_TOKEN = "AuthToken";
	private static final String ARG_TERM = "Term";
	private static final String ARG_USERNAME = "Username";
	
	public static Bundle bundleArgs(String authToken, String term, String username) {
		Bundle res = new Bundle();
		res.putString(ARG_AUTH_TOKEN, authToken);
		res.putString(ARG_TERM, term);
		res.putString(ARG_USERNAME, username);
		return res;
	}
	
	public static LoadUserSchedule getInstance(LoaderManager loaderManager, int id) {
		Loader<AsyncLoaderResult<PersonScheduleWeek>> res = loaderManager.getLoader(id);
		return (LoadUserSchedule)res;
	}

	private String mAuthToken;
	private String mTerm;
	private String mUsername;
	
	public LoadUserSchedule(Context context, Bundle args) {
		super(context);
		mAuthToken = args.getString(ARG_AUTH_TOKEN);
		mTerm = args.getString(ARG_TERM);
		mUsername = args.getString(ARG_USERNAME);
	}

	public String getAuthToken() {
		return mAuthToken;
	}
	
	@Override
	protected PersonScheduleWeek doInBackground() throws AsyncLoaderException {
		Log.d(C.TAG, "Starting LoadUserSchedule");
		
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
				return service.getUserSchedule(mAuthToken, mTerm, mUsername);

			} catch (AuthenticationException e) {
				Log.w(C.TAG, "Invalid auth token");
				throw new InvalidAuthTokenException();
				
			} catch (ClientException e) {
				Log.e(C.TAG, "Client request failed", e);
				throw new AsyncLoaderException(e.getMessage());
				
			} catch (ServerException e) {
				Log.e(C.TAG, "Server request failed", e);
				throw new AsyncLoaderException(getContext().getString(R.string.error_server));
				
			} catch (JSONException e) {
				Log.e(C.TAG, "An error occured while parsing the JSON response", e);
				throw new AsyncLoaderException(getContext().getString(R.string.error_json));
				
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
