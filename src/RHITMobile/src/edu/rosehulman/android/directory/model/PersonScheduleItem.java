package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonScheduleItem implements Parcelable {
	public String course;
	public String courseName;
	public int section;
	public int hourStart;
	public int hourEnd;
	public String room;
	
	public PersonScheduleItem() {
		
	}
	
	public PersonScheduleItem(String course, String courseName, int section, int hourStart, int hourEnd, String room) {
		this.course = course;
		this.courseName = courseName;
		this.section = section;
		this.hourStart = hourStart;
		this.hourEnd = hourEnd;
		this.room = room;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(course);
		dest.writeString(courseName);
		dest.writeInt(section);
		dest.writeInt(hourStart);
		dest.writeInt(hourEnd);
		dest.writeString(room);
	}

	public static final Parcelable.Creator<PersonScheduleItem> CREATOR = new Parcelable.Creator<PersonScheduleItem>() {

		@Override
		public PersonScheduleItem createFromParcel(Parcel in) {
			PersonScheduleItem res = new PersonScheduleItem();
			
			res.course = in.readString();
			res.courseName = in.readString();
			res.section = in.readInt();
			res.hourStart = in.readInt();
			res.hourEnd = in.readInt();
			res.room = in.readString();
			
			return res;
		}

		@Override
		public PersonScheduleItem[] newArray(int size) {
			return new PersonScheduleItem[size];
		}
	};
}