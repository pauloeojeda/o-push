package org.obm.push.data;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IApplicationData;
import org.obm.push.impl.SyncHandler;
import org.obm.push.utils.Base64;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CalendarDecoder extends Decoder implements IDataDecoder {
	
	@Override
	public IApplicationData decode(Element syncData) {
		Element containerNode;
		Calendar calendar = new Calendar();
		
		// Main attributes
		
		calendar.setOrganizerName(parseDOMString(DOMUtils.getUniqueElement(syncData, "OrganizerName")));
		calendar.setOrganizerEmail(parseDOMString(DOMUtils.getUniqueElement(syncData, "OrganizerEmail")));
		calendar.setLocation(parseDOMString(DOMUtils.getUniqueElement(syncData, "Location")));
		calendar.setSubject(parseDOMString(DOMUtils.getUniqueElement(syncData, "Subject")));
		calendar.setUID(parseDOMString(DOMUtils.getUniqueElement(syncData, "UID")));
		calendar.setDtStamp(parseDOMDate(DOMUtils.getUniqueElement(syncData, "DtStamp")));
		calendar.setEndTime(parseDOMDate(DOMUtils.getUniqueElement(syncData, "EndTime")));
		calendar.setStartTime(parseDOMDate(DOMUtils.getUniqueElement(syncData, "StartTime")));
		calendar.setAllDayEvent(parseDOMByte(DOMUtils.getUniqueElement(syncData, "AllDayEvent")));
		calendar.setBusyStatus(parseDOMByte(DOMUtils.getUniqueElement(syncData, "BusyStatus")));
		calendar.setSensitivity(parseDOMByte(DOMUtils.getUniqueElement(syncData, "Sensitivity")));
		calendar.setMeetingStatus(parseDOMByte(DOMUtils.getUniqueElement(syncData, "MeetingStatus")));
		calendar.setReminder(parseDOMInt(DOMUtils.getUniqueElement(syncData, "Reminder")));
		calendar.setCategories(parseDOMStringCollection(DOMUtils.getUniqueElement(syncData, "Categories"), "Category"));
		
		calendar.setTimeZone(parseDOMTimeZone(DOMUtils.getUniqueElement(syncData, "TimeZone")));
		
		// Components attributes
		
		// Attendees
		
		containerNode = DOMUtils.getUniqueElement(syncData, "Attendees");
		if (containerNode != null) {
			ArrayList<Attendee> attendees = new ArrayList<Attendee>();
			for (int i = 0, n = containerNode.getChildNodes().getLength(); i < n; i += 1) {
				Element subnode = (Element) containerNode.getChildNodes().item(i);
				Attendee attendee = new Attendee();
				
				attendee.setEmail(parseDOMString(DOMUtils.getUniqueElement(subnode, "Email")));
				attendee.setName(parseDOMString(DOMUtils.getUniqueElement(subnode, "Name")));
				attendee.setAttendeeStatus(parseDOMByte(DOMUtils.getUniqueElement(subnode, "AttendeeStatus")));
				attendee.setAttendeeType(parseDOMByte(DOMUtils.getUniqueElement(subnode, "AttendeeType")));
			}
			calendar.setAttendees(attendees);
		}

		
		// Exceptions

		containerNode = DOMUtils.getUniqueElement(syncData, "Exceptions");
		if (containerNode != null) {
			ArrayList<Exception> exceptions = new ArrayList<Exception>();
			for (int i = 0, n = containerNode.getChildNodes().getLength(); i < n; i += 1) {
				Element subnode = (Element) containerNode.getChildNodes().item(i);
				Exception exception = new Exception();
				
				exception.setSubject(parseDOMString(DOMUtils.getUniqueElement(subnode, "Subject")));
				exception.setLocation(parseDOMString(DOMUtils.getUniqueElement(subnode, "Location")));
				exception.setExceptionStartTime(parseDOMDate(DOMUtils.getUniqueElement(subnode, "ExceptionStartTime")));
				exception.setStartTime(parseDOMDate(DOMUtils.getUniqueElement(subnode, "StartTime")));
				exception.setEndTime(parseDOMDate(DOMUtils.getUniqueElement(subnode, "EndTime")));
				exception.setDtStamp(parseDOMDate(DOMUtils.getUniqueElement(subnode, "DtStamp")));
				exception.setDeleted(parseDOMByte(DOMUtils.getUniqueElement(subnode, "Deleted")));
				exception.setSensitivity(parseDOMByte(DOMUtils.getUniqueElement(subnode, "Sensitivity")));
				exception.setBusyStatus(parseDOMByte(DOMUtils.getUniqueElement(subnode, "BusyStatus")));
				exception.setAllDayEvent(parseDOMByte(DOMUtils.getUniqueElement(subnode, "AllDayEvent")));
				exception.setMeetingStatus(parseDOMByte(DOMUtils.getUniqueElement(subnode, "MeetingStatus")));
				exception.setReminder(parseDOMInt(DOMUtils.getUniqueElement(subnode, "Reminder")));
				exception.setCategories(parseDOMStringCollection(DOMUtils.getUniqueElement(subnode, "Categories"), "Category"));
			}
			calendar.setExceptions(exceptions);
		}
		
		// Recurrence
		containerNode = DOMUtils.getUniqueElement(syncData, "Recurrence");
		if (containerNode != null) {
			Recurrence recurrence = new Recurrence();
			
			recurrence.setUntil(parseDOMDate(DOMUtils.getUniqueElement(containerNode, "Recurrence_Until")));
			recurrence.setType(parseDOMByte(DOMUtils.getUniqueElement(containerNode, "Recurrence_Type")));
			recurrence.setWeekOfMonth(parseDOMByte(DOMUtils.getUniqueElement(containerNode, "Recurrence_WeekOfMonth")));
			recurrence.setMonthOfYear(parseDOMByte(DOMUtils.getUniqueElement(containerNode, "Recurrence_MonthOfYear")));
			recurrence.setDayOfMonth(parseDOMByte(DOMUtils.getUniqueElement(containerNode, "Recurrence_DayOfMonth")));
			calendar.setRecurrence(recurrence);
		}
		
		parseTimeZone(DOMUtils.getUniqueElement(syncData, "TimeZone").getTextContent());
		
		return null;
	}

}
