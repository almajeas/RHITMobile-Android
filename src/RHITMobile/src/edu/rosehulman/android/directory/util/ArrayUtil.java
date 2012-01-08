package edu.rosehulman.android.directory.util;

/**
 * Useful functions for arrays
 */
public class ArrayUtil {
    
	/**
	 * Converts an array to a different type
	 * 
	 * @param <T> The goal type
	 * @param <V> The source type
	 * @param arr The array to convert
	 * @param out An array of the proper type with the same size
	 * 
	 * @return For convenience, out
	 */
    @SuppressWarnings("unchecked")
	public static <T,V> T[] cast(V[] arr, T[] out) {
    	for (int i = 0; i < arr.length; i++) {
			out[i] = (T)arr[i];
		}
    	return out;
    }
	
}
