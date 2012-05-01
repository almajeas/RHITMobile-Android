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
import edu.rosehulman.android.directory.model.RoomScheduleDay;
import edu.rosehulman.android.directory.model.RoomScheduleItem;
import edu.rosehulman.android.directory.model.RoomScheduleWeek;
import edu.rosehulman.android.directory.service.AuthenticationException;
import edu.rosehulman.android.directory.service.ClientException;
import edu.rosehulman.android.directory.service.MobileDirectoryService;
import edu.rosehulman.android.directory.service.ServerException;

public class LoadRoomSchedule extends CachedAsyncLoader<RoomScheduleWeek> {
	
	private static final String ARG_AUTH_TOKEN = "AuthToken";
	private static final String ARG_TERM = "Term";
	private static final String ARG_ROOM = "Room";
	
	public static Bundle bundleArgs(String authToken, String term, String room) {
		Bundle res = new Bundle();
		res.putString(ARG_AUTH_TOKEN, authToken);
		res.putString(ARG_TERM, term);
		res.putString(ARG_ROOM, room);
		return res;
	}
	
	public static LoadRoomSchedule getInstance(LoaderManager loaderManager, int id) {
		Loader<AsyncLoaderResult<RoomScheduleWeek>> res = loaderManager.getLoader(id);
		return (LoadRoomSchedule)res;
	}

	private String mAuthToken;
	private String mTerm;
	private String mRoom;
	
	public LoadRoomSchedule(Context context, Bundle args) {
		super(context);
		mAuthToken = args.getString(ARG_AUTH_TOKEN);
		mTerm = args.getString(ARG_TERM);
		mRoom = args.getString(ARG_ROOM);
	}

	public String getAuthToken() {
		return mAuthToken;
	}
	
	@Override
	protected RoomScheduleWeek doInBackground() throws AsyncLoaderException {
		Log.d(C.TAG, "Starting LoadRoomSchedule");
		
		//get the person's course schedules
		MobileDirectoryService service = new MobileDirectoryService();
		CoursesResponse response = loadCourses(service);
		
		if (response == null) {
			return null;
		}

		//convert the course schedules to a user schedule
		RoomScheduleWeek schedule = new RoomScheduleWeek();
		
		for (Course course : response.courses) {
			for (CourseMeeting meeting : course.schedule) {
				RoomScheduleDay day = schedule.getDay(meeting.day);
				RoomScheduleItem item;
				item = new RoomScheduleItem(course.crn, course.course, course.title, meeting.startPeriod, meeting.endPeriod);
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
				return service.getRoomSchedule(mAuthToken, mTerm, mRoom);

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
