package edu.rosehulman.android.directory.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class PersonScheduleDay implements Parcelable {
	
	public List<PersonScheduleItem> mItems;

	public PersonScheduleDay() {
		mItems = new ArrayList<PersonScheduleItem>();
	}
	
	public PersonScheduleDay(PersonScheduleItem[] items) {
		this.mItems = new ArrayList<PersonScheduleItem>(Arrays.asList(items));
	}
	
	public boolean isEmpty() {
		return mItems.isEmpty();
	}
	
	public void addItem(PersonScheduleItem item) {
		mItems.add(item);
		Collections.sort(mItems);
	}
	
	public int count() {
		return mItems.size();
	}
	
	public PersonScheduleItem getItem(int index) {
		return mItems.get(index);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(mItems);
	}
	
	public PersonScheduleDay(Parcel in) {
		mItems = new ArrayList<PersonScheduleItem>();
		in.readTypedList(mItems, PersonScheduleItem.CREATOR);
	}

	public static final Parcelable.Creator<PersonScheduleDay> CREATOR = new Parcelable.Creator<PersonScheduleDay>() {

		@Override
		public PersonScheduleDay createFromParcel(Parcel in) {
			return new PersonScheduleDay(in);
		}

		@Override
		public PersonScheduleDay[] newArray(int size) {
			return new PersonScheduleDay[size];
		}
	};
}