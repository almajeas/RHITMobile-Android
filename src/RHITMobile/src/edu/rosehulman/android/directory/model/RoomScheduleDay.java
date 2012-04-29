package edu.rosehulman.android.directory.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class RoomScheduleDay implements Parcelable {
	
	public List<RoomScheduleItem> mItems;

	public RoomScheduleDay() {
		mItems = new ArrayList<RoomScheduleItem>();
	}
	
	public RoomScheduleDay(RoomScheduleItem[] items) {
		this.mItems = new ArrayList<RoomScheduleItem>(Arrays.asList(items));
	}
	
	public boolean isEmpty() {
		return mItems.isEmpty();
	}
	
	public void addItem(RoomScheduleItem item) {
		mItems.add(item);
		Collections.sort(mItems);
	}
	
	public int count() {
		return mItems.size();
	}
	
	public RoomScheduleItem getItem(int index) {
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
	
	public RoomScheduleDay(Parcel in) {
		mItems = new ArrayList<RoomScheduleItem>();
		in.readTypedList(mItems, RoomScheduleItem.CREATOR);
	}

	public static final Parcelable.Creator<RoomScheduleDay> CREATOR = new Parcelable.Creator<RoomScheduleDay>() {

		@Override
		public RoomScheduleDay createFromParcel(Parcel in) {
			return new RoomScheduleDay(in);
		}

		@Override
		public RoomScheduleDay[] newArray(int size) {
			return new RoomScheduleDay[size];
		}
	};
}
