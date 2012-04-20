package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonScheduleItem implements Comparable<PersonScheduleItem>, Parcelable {
	public int crn;
	
	public String course;
	public String courseName;
	public int hourStart;
	public int hourEnd;
	public String room;
	
	public PersonScheduleItem() {
		
	}
	
	public PersonScheduleItem(int crn, String course, String courseName, int hourStart, int hourEnd, String room) {
		this.crn = crn;
		this.course = course;
		this.courseName = courseName;
		this.hourStart = hourStart;
		this.hourEnd = hourEnd;
		this.room = room;
	}
	
	@Override
	public int compareTo(PersonScheduleItem other) {
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
		dest.writeString(room);
	}
	
	public PersonScheduleItem(Parcel in) {
		crn = in.readInt();
		course = in.readString();
		courseName = in.readString();
		hourStart = in.readInt();
		hourEnd = in.readInt();
		room = in.readString();
	}

	public static final Parcelable.Creator<PersonScheduleItem> CREATOR = new Parcelable.Creator<PersonScheduleItem>() {

		@Override
		public PersonScheduleItem createFromParcel(Parcel in) {
			return new PersonScheduleItem(in);
		}

		@Override
		public PersonScheduleItem[] newArray(int size) {
			return new PersonScheduleItem[size];
		}
	};
}