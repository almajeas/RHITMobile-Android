package edu.rosehulman.android.directory.model;

import java.util.HashMap;

public enum DirectionActionType {
	
	/** No associated action */
	NONE,
	
	/** Keep doing what you were doing */
	GO_STRAIGHT,
	/** Look both ways first */
	CROSS_STREET,
	/** Follow the yellow brick road */
	FOLLOW_PATH,
	
	/** Turn left a little */
	SLIGHT_LEFT,
	/** Turn right a little */
	SLIGHT_RIGHT,
	/** Turn left */
	TURN_LEFT,
	/** Turn right */
	TURN_RIGHT,
	/** Turn left a lot */
	SHARP_LEFT,
	/** Turn right a lot */
	SHARP_RIGHT,
	
	/** Enter the building */
	ENTER_BUILDING,
	/** Exit the building */
	EXIT_BUILDING,
	/** Walk up the stairs */
	ASCEND_STAIRS,
	/** Go down the stairs */
	DESCEND_STAIRS;
	
	private static HashMap<Integer, DirectionActionType> types;
	
	static {
		types = new HashMap<Integer, DirectionActionType>();
		for (DirectionActionType type : DirectionActionType.values()) {
			types.put(type.ordinal(), type);
		}
	}
	
	public static DirectionActionType fromOrdinal(int ordinal) {
		return types.get(ordinal);
	}
}
