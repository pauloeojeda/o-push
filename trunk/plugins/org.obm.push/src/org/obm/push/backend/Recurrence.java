package org.obm.push.backend;

import java.text.SimpleDateFormat;

import org.obm.push.data.calendarenum.RecurrenceType;

public class Recurrence {
	private SimpleDateFormat until;
	private RecurrenceType type;
	private Integer weekOfMonth;
	private Integer monthOfYear;
	private Integer dayOfMonth;
	private Integer occurrences;
	private Integer interval;
	private Integer dayOfWeek;
	
	public SimpleDateFormat getUntil() {
		return until;
	}
	public void setUntil(SimpleDateFormat until) {
		this.until = until;
	}
	public RecurrenceType getType() {
		return type;
	}
	public void setType(RecurrenceType type) {
		this.type = type;
	}
	public Integer getWeekOfMonth() {
		return weekOfMonth;
	}
	public void setWeekOfMonth(Integer weekOfMonth) {
		this.weekOfMonth = weekOfMonth;
	}
	public Integer getMonthOfYear() {
		return monthOfYear;
	}
	public void setMonthOfYear(Integer monthOfYear) {
		this.monthOfYear = monthOfYear;
	}
	public Integer getDayOfMonth() {
		return dayOfMonth;
	}
	public void setDayOfMonth(Integer dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
	public Integer getOccurrences() {
		return occurrences;
	}
	public void setOccurrences(Integer occurrences) {
		this.occurrences = occurrences;
	}
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
	}
	public Integer getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(Integer dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
}
