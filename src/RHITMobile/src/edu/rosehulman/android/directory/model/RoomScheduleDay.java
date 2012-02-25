package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomScheduleDay implements Parcelable {
	
	public RoomScheduleItem[] items;

	public RoomScheduleDay(RoomScheduleItem[] items) {
		this.items = items;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedArray(items, flags);
	}

	public static final Parcelable.Creator<RoomScheduleDay> CREATOR = new Parcelable.Creator<RoomScheduleDay>() {

		@Override
		public RoomScheduleDay createFromParcel(Parcel in) {
			return new RoomScheduleDay(in.createTypedArray(RoomScheduleItem.CREATOR));
		}

		@Override
		public RoomScheduleDay[] newArray(int size) {
			return new RoomScheduleDay[size];
		}
	};
}