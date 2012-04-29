package edu.rosehulman.android.directory.util;

/**
 * Assorted useful time operations
 */
public class TimeUtil {

	/**
	 * Formats a military time integer to a 12 hour human readable string
	 * 
	 * @param time The number of elapsed minutes in the day
	 * @return A form understandable by a human
	 */
	public static String formatTime(int time) {
		int hours = time / 100;
		int minutes = time % 100;
		
		String m = "am";
		if (hours == 12) {
			m = "pm";
		} else if (hours > 12) {
			hours -= 12;
			m = "pm";
		}
		
		if (minutes == 0) {
			return String.format("%d%s", hours, m);
		} else {
			return String.format("%d:%02d%s", hours, minutes, m);
		}
	}
}
