package org.obm.push.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.obm.push.data.calendarenum.CalendarBusyStatus;
import org.obm.push.data.calendarenum.CalendarMeetingStatus;
import org.obm.push.data.calendarenum.CalendarSensitivity;

public class Exception implements IEventCalendar {
	private String subject;
	private String location;
	private SimpleDateFormat exceptionStartTime;
	private SimpleDateFormat startTime;
	private SimpleDateFormat endTime;
	private SimpleDateFormat dtStamp;
	private Boolean deleted;
	private CalendarSensitivity sensitivity;
	private CalendarBusyStatus busyStatus;
	private Boolean allDayEvent;
	private CalendarMeetingStatus meetingStatus;
	private Integer reminder;
	private ArrayList<String> categories;
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public SimpleDateFormat getExceptionStartTime() {
		return exceptionStartTime;
	}
	public void setExceptionStartTime(SimpleDateFormat exceptionStartTime) {
		this.exceptionStartTime = exceptionStartTime;
	}
	public SimpleDateFormat getStartTime() {
		return startTime;
	}
	public void setStartTime(SimpleDateFormat startTime) {
		this.startTime = startTime;
	}
	public SimpleDateFormat getEndTime() {
		return endTime;
	}
	public void setEndTime(SimpleDateFormat endTime) {
		this.endTime = endTime;
	}
	public SimpleDateFormat getDtStamp() {
		return dtStamp;
	}
	public void setDtStamp(SimpleDateFormat dtStamp) {
		this.dtStamp = dtStamp;
	}
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public CalendarSensitivity getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(CalendarSensitivity sensitivity) {
		this.sensitivity = sensitivity;
	}
	public CalendarBusyStatus getBusyStatus() {
		return busyStatus;
	}
	public void setBusyStatus(CalendarBusyStatus busyStatus) {
		this.busyStatus = busyStatus;
	}
	public Boolean getAllDayEvent() {
		return allDayEvent;
	}
	public void setAllDayEvent(Boolean allDayEvent) {
		this.allDayEvent = allDayEvent;
	}
	public CalendarMeetingStatus getMeetingStatus() {
		return meetingStatus;
	}
	public void setMeetingStatus(CalendarMeetingStatus meetingStatus) {
		this.meetingStatus = meetingStatus;
	}
	public Integer getReminder() {
		return reminder;
	}
	public void setReminder(Integer reminder) {
		this.reminder = reminder;
	}
	public ArrayList<String> getCategories() {
		return categories;
	}
	public void setCategories(ArrayList<String> categories) {
		this.categories = categories;
	}
}
