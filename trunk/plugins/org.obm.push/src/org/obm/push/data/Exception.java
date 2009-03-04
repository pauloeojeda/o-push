package org.obm.push.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Exception {
	private String subject;
	private String location;
	private SimpleDateFormat exceptionStartTime;
	private SimpleDateFormat startTime;
	private SimpleDateFormat endTime;
	private SimpleDateFormat dtStamp;
	private Byte deleted;
	private Byte sensitivity;
	private Byte busyStatus;
	private Byte allDayEvent;
	private Byte meetingStatus;
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
	public Byte getDeleted() {
		return deleted;
	}
	public void setDeleted(Byte deleted) {
		this.deleted = deleted;
	}
	public Byte getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(Byte sensitivity) {
		this.sensitivity = sensitivity;
	}
	public Byte getBusyStatus() {
		return busyStatus;
	}
	public void setBusyStatus(Byte busyStatus) {
		this.busyStatus = busyStatus;
	}
	public Byte getAllDayEvent() {
		return allDayEvent;
	}
	public void setAllDayEvent(Byte allDayEvent) {
		this.allDayEvent = allDayEvent;
	}
	public Byte getMeetingStatus() {
		return meetingStatus;
	}
	public void setMeetingStatus(Byte meetingStatus) {
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
