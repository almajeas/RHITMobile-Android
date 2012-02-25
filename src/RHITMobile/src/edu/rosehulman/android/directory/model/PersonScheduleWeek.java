package edu.rosehulman.android.directory.model;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonScheduleWeek implements Parcelable {
	
	public String[] tags;
	public PersonScheduleDay[] days;
	
	public PersonScheduleWeek() {
	}
	
	public PersonScheduleWeek(String[] tags, PersonScheduleDay[] days) {
		this.tags = tags;
		this.days = days;
	}
	
	public PersonScheduleDay getDay(String tag) {
		return days[Arrays.asList(tags).indexOf(tag)];
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(tags);
		dest.writeTypedArray(days, flags);
	}
	
	public static Parcelable.Creator<PersonScheduleWeek> CREATOR = new Parcelable.Creator<PersonScheduleWeek>() {

		@Override
		public PersonScheduleWeek createFromParcel(Parcel source) {
			PersonScheduleWeek res = new PersonScheduleWeek();
			res.tags = source.createStringArray();
			res.days = source.createTypedArray(PersonScheduleDay.CREATOR);
			return res;
		}

		@Override
		public PersonScheduleWeek[] newArray(int size) {
			return new PersonScheduleWeek[size];
		}
		
	};
}