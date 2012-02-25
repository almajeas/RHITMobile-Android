package edu.rosehulman.android.directory.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonScheduleDay implements Parcelable {
	
	public PersonScheduleItem[] items;

	public PersonScheduleDay(PersonScheduleItem[] items) {
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

	public static final Parcelable.Creator<PersonScheduleDay> CREATOR = new Parcelable.Creator<PersonScheduleDay>() {

		@Override
		public PersonScheduleDay createFromParcel(Parcel in) {
			return new PersonScheduleDay(in.createTypedArray(PersonScheduleItem.CREATOR));
		}

		@Override
		public PersonScheduleDay[] newArray(int size) {
			return new PersonScheduleDay[size];
		}
	};
}