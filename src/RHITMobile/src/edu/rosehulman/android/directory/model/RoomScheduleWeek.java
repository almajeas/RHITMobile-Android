package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomScheduleWeek implements Parcelable {
	
	public RoomScheduleDay[] days;
	
	public RoomScheduleWeek() {
		days = new RoomScheduleDay[ScheduleDay.values().length];
		for (int i = 0; i < days.length; i++) {
			days[i] = new RoomScheduleDay();
		}
	}
	
	public boolean isEmpty() {
		for (ScheduleDay day : ScheduleDay.values())
			if (hasDay(day))
				return false;
		return true;
	}
	
	public boolean hasDay(ScheduleDay day) {
		return !getDay(day).isEmpty();
	}
	
	public RoomScheduleDay getDay(ScheduleDay day) {
		return days[day.ordinal()];
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedArray(days, flags);
	}
	
	public RoomScheduleWeek(Parcel in) {
		days = in.createTypedArray(RoomScheduleDay.CREATOR);
	}

	public static Parcelable.Creator<RoomScheduleWeek> CREATOR =
			new Parcelable.Creator<RoomScheduleWeek>() {

		@Override
		public RoomScheduleWeek createFromParcel(Parcel source) {
			return new RoomScheduleWeek(source);
		}

		@Override
		public RoomScheduleWeek[] newArray(int size) {
			return new RoomScheduleWeek[size];
		}
		
	};
}
