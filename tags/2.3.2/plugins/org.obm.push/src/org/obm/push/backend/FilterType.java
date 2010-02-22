package org.obm.push.backend;

public enum FilterType {
	
	ALL_ITEMS,//0
	ONE_DAY_BACK,//1
	THREE_DAYS_BACK,//2
	ONE_WEEK_BACK,//3
	TWO_WEEKS_BACK,//4
	ONE_MONTHS_BACK,//5
	THREE_MONTHS_BACK,//6
	SIX_MONTHS_BACK,//7
	FILTER_BY_NO_INCOMPLETE_TASKS;//8
	
	public static FilterType getFilterType(String number){
		if("0".equals(number)){
			return ALL_ITEMS;
		} else if("1".equals(number)){
			return ONE_DAY_BACK;
		} else if("2".equals(number)){
			return THREE_DAYS_BACK;
		} else if("3".equals(number)){
			return ONE_WEEK_BACK;
		} else if("4".equals(number)){
			return TWO_WEEKS_BACK;
		} else if("5".equals(number)){
			return ONE_MONTHS_BACK;
		} else if("6".equals(number)){
			return THREE_MONTHS_BACK;
		} else if("7".equals(number)){
			return SIX_MONTHS_BACK;
		} else if("8".equals(number)){
			return FILTER_BY_NO_INCOMPLETE_TASKS;
		} else {
			return ALL_ITEMS;
		}
	}
}
