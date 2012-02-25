package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomScheduleItem implements Parcelable {
	public String course;
	public String courseName;
	public int section;
	public int hourStart;
	public int hourEnd;
	
	public RoomScheduleItem() {
		
	}
	
	public RoomScheduleItem(String course, String courseName, int section, int hourStart, int hourEnd) {
		this.course = course;
		this.courseName = courseName;
		this.section = section;
		this.hourStart = hourStart;
		this.hourEnd = hourEnd;
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
	}

	public static final Parcelable.Creator<RoomScheduleItem> CREATOR = new Parcelable.Creator<RoomScheduleItem>() {

		@Override
		public RoomScheduleItem createFromParcel(Parcel in) {
			RoomScheduleItem res = new RoomScheduleItem();
			
			res.course = in.readString();
			res.courseName = in.readString();
			res.section = in.readInt();
			res.hourStart = in.readInt();
			res.hourEnd = in.readInt();
			
			return res;
		}

		@Override
		public RoomScheduleItem[] newArray(int size) {
			return new RoomScheduleItem[size];
		}
	};
}