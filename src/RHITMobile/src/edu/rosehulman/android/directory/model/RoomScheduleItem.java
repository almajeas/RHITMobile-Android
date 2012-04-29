package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomScheduleItem implements Comparable<RoomScheduleItem>, Parcelable {
	public int crn;
	
	public String course;
	public String courseName;
	public int hourStart;
	public int hourEnd;
	
	public RoomScheduleItem() {
		
	}
	
	public RoomScheduleItem(int crn, String course, String courseName, int hourStart, int hourEnd) {
		this.crn = crn;
		this.course = course;
		this.courseName = courseName;
		this.hourStart = hourStart;
		this.hourEnd = hourEnd;
	}
	
	@Override
	public int compareTo(RoomScheduleItem other) {
		if (hourStart < other.hourStart)
			return -1;
		else if (hourStart > other.hourStart)
			return 1;
		else
			return 0;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(crn);
		dest.writeString(course);
		dest.writeString(courseName);
		dest.writeInt(hourStart);
		dest.writeInt(hourEnd);
	}
	
	public RoomScheduleItem(Parcel in) {
		crn = in.readInt();
		course = in.readString();
		courseName = in.readString();
		hourStart = in.readInt();
		hourEnd = in.readInt();
	}

	public static final Parcelable.Creator<RoomScheduleItem> CREATOR = new Parcelable.Creator<RoomScheduleItem>() {

		@Override
		public RoomScheduleItem createFromParcel(Parcel in) {
			return new RoomScheduleItem(in);
		}

		@Override
		public RoomScheduleItem[] newArray(int size) {
			return new RoomScheduleItem[size];
		}
	};
}
