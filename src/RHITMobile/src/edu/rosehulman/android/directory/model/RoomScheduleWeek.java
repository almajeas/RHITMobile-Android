package edu.rosehulman.android.directory.model;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomScheduleWeek implements Parcelable {
	
	public String[] tags;
	public RoomScheduleDay[] days;
	
	public RoomScheduleWeek() {
	}
	
	public RoomScheduleWeek(String[] tags, RoomScheduleDay[] days) {
		this.tags = tags;
		this.days = days;
	}
	
	public RoomScheduleDay getDay(String tag) {
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
	
	public static Parcelable.Creator<RoomScheduleWeek> CREATOR = new Parcelable.Creator<RoomScheduleWeek>() {

		@Override
		public RoomScheduleWeek createFromParcel(Parcel source) {
			RoomScheduleWeek res = new RoomScheduleWeek();
			res.tags = source.createStringArray();
			res.days = source.createTypedArray(RoomScheduleDay.CREATOR);
			return res;
		}

		@Override
		public RoomScheduleWeek[] newArray(int size) {
			return new RoomScheduleWeek[size];
		}
		
	};
}