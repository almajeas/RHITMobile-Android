package edu.rosehulman.android.directory.model;

import java.util.HashMap;

/**
 * Day of a week related to schedules
 */
public enum ScheduleDay {
	
	MONDAY,
	TUESDAY,
	WEDNESDAY,
	THURSDAY,
	FRIDAY,
	SATURDAY;
	
	private static HashMap<Integer, ScheduleDay> types;
	
	static {
		types = new HashMap<Integer, ScheduleDay>();
		for (ScheduleDay type : ScheduleDay.values()) {
			types.put(type.ordinal(), type);
		}
	}
	
	public static ScheduleDay fromOrdinal(int ordinal) {
		return types.get(ordinal);
	}
}
