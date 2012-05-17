package edu.rosehulman.android.directory.model;

import java.util.Calendar;
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
	
	public static ScheduleDay today() {
		switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			return MONDAY;
		case Calendar.TUESDAY:
			return TUESDAY;
		case Calendar.WEDNESDAY:
			return WEDNESDAY;
		case Calendar.THURSDAY:
			return THURSDAY;
		case Calendar.FRIDAY:
			return FRIDAY;
		case Calendar.SATURDAY:
			return SATURDAY;
		default:
			return MONDAY;
		}
	}
}
