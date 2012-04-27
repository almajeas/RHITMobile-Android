package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonScheduleWeek implements Parcelable {
	
	public PersonScheduleDay[] days;
	
	public PersonScheduleWeek() {
		days = new PersonScheduleDay[ScheduleDay.values().length];
		for (int i = 0; i < days.length; i++) {
			days[i] = new PersonScheduleDay();
		}
	}
	
	public boolean hasDay(ScheduleDay day) {
		return !getDay(day).isEmpty();
	}
	
	public PersonScheduleDay getDay(ScheduleDay day) {
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
	
	public PersonScheduleWeek(Parcel in) {
		days = in.createTypedArray(PersonScheduleDay.CREATOR);
	}
	
	public static Parcelable.Creator<PersonScheduleWeek> CREATOR =
			new Parcelable.Creator<PersonScheduleWeek>() {

		@Override
		public PersonScheduleWeek createFromParcel(Parcel source) {
			return new PersonScheduleWeek(source);
		}

		@Override
		public PersonScheduleWeek[] newArray(int size) {
			return new PersonScheduleWeek[size];
		}
		
	};
}