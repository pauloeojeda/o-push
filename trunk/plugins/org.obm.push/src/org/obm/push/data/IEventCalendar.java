package org.obm.push.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import org.obm.push.data.calendarenum.CalendarBusyStatus;
import org.obm.push.data.calendarenum.CalendarMeetingStatus;
import org.obm.push.data.calendarenum.CalendarSensitivity;

public interface IEventCalendar {
	public SimpleDateFormat getDtStamp();
	public void setDtStamp(SimpleDateFormat dtStamp);
	public String getLocation();
	public void setLocation(String location);
	public String getSubject();
	public void setSubject(String subject);
	public SimpleDateFormat getEndTime();
	public void setEndTime(SimpleDateFormat endTime);
	public SimpleDateFormat getStartTime();
	public void setStartTime(SimpleDateFormat startTime);
	public Boolean getAllDayEvent();
	public void setAllDayEvent(Boolean allDayEvent);
	public CalendarBusyStatus getBusyStatus();
	public void setBusyStatus(CalendarBusyStatus busyStatus);
	public CalendarSensitivity getSensitivity();
	public void setSensitivity(CalendarSensitivity sensitivity);
	public CalendarMeetingStatus getMeetingStatus();
	public void setMeetingStatus(CalendarMeetingStatus meetingStatus);
	public Integer getReminder();
	public void setReminder(Integer reminder);
	public ArrayList<String> getCategories();
	public void setCategories(ArrayList<String> categories);
}
