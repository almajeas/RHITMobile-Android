package edu.rosehulman.android.directory.util;

/**
 * Useful functions for arrays
 */
public class ArrayUtil {
    
	/**
	 * Converts an array to a different type
	 * 
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
	
    
    /**
     * Joins an array with a separator
     * 
     * @param arr The array
     * @param sep The separator between elements in the array
     * @return The string representation of the joined array
     */
    public static <T> String join(T[] arr, String sep) {
    	StringBuilder res = new StringBuilder();
    	for (T o : arr) {
    		res.append(o);
    		res.append(sep);
    	}
    	res.delete(res.length()-sep.length(), res.length());
    	return res.toString();
    }
    
    /**
     * Joins an array with a separator
     * 
     * @param arr The array
     * @param sep The separator between elements in the array
     * @return The string representation of the joined array
     */
    public static String join(long[] arr, String sep) {
    	StringBuilder res = new StringBuilder();
    	for (long o : arr) {
    		res.append(o);
    		res.append(sep);
    	}
    	res.delete(res.length()-sep.length(), res.length());
    	return res.toString();
    }
    
    /**
     * Finds the given object in the given array
     * 
     * @param arr The array to search linearly
     * @param o The object to find
     * @return The index of the object, or -1 if not found
     */
    public static <T> int indexOf(T[] arr, T o) {
    	for (int i = 0; i < arr.length; i++) {
			if (o.equals(arr[i])) {
				return i;
			}
		}
    	return -1;
    }
}
