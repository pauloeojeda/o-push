package org.obm.push.data;

import java.text.SimpleDateFormat;

public class Recurrence {
	private SimpleDateFormat until;
	private Byte type;
	private Byte weekOfMonth;
	private Byte monthOfYear;
	private Byte dayOfMonth;
	public SimpleDateFormat getUntil() {
		return until;
	}
	public void setUntil(SimpleDateFormat until) {
		this.until = until;
	}
	public Byte getType() {
		return type;
	}
	public void setType(Byte type) {
		this.type = type;
	}
	public Byte getWeekOfMonth() {
		return weekOfMonth;
	}
	public void setWeekOfMonth(Byte weekOfMonth) {
		this.weekOfMonth = weekOfMonth;
	}
	public Byte getMonthOfYear() {
		return monthOfYear;
	}
	public void setMonthOfYear(Byte monthOfYear) {
		this.monthOfYear = monthOfYear;
	}
	public Byte getDayOfMonth() {
		return dayOfMonth;
	}
	public void setDayOfMonth(Byte dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
}
