package org.obm.push.data.calendarenum;

public enum RecurrenceDayOfWeek {
	  SUNDAY,
	  MONDAY,
	  TUESDAY,
	  WEDNESDAY,
	  THURSDAY,
	  FRIDAY,
	  SATURDAY,
	  ;
  
	public int asInt() {
		switch (this) {
		case SUNDAY:
			return 1;
		case MONDAY:
			return 2;
		case TUESDAY:
			return 4;
		case WEDNESDAY:
			return 8;
		case THURSDAY:
			return 16;
		case FRIDAY:
			return 32;
		case SATURDAY:
			return 64;
		default:
			return 0;
		}
	}
}
