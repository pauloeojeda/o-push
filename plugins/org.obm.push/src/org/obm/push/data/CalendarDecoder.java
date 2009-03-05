package org.obm.push.data;

import java.util.ArrayList;

import org.obm.push.backend.IApplicationData;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.data.calendarenum.AttendeeType;
import org.obm.push.data.calendarenum.CalendarBusyStatus;
import org.obm.push.data.calendarenum.CalendarMeetingStatus;
import org.obm.push.data.calendarenum.CalendarSensitivity;
import org.obm.push.impl.SyncHandler;
import org.obm.push.utils.Base64;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class CalendarDecoder extends Decoder implements IDataDecoder {
	
	@Override
	public IApplicationData decode(Element syncData) {
		Element containerNode;
		Calendar calendar = new Calendar();
		
		// Main attributes
		
		calendar.setOrganizerName(parseDOMString(DOMUtils.getUniqueElement(syncData, "OrganizerName")));
		calendar.setOrganizerEmail(parseDOMString(DOMUtils.getUniqueElement(syncData, "OrganizerEmail")));
		calendar.setUID(parseDOMString(DOMUtils.getUniqueElement(syncData, "UID")));
		calendar.setTimeZone(parseDOMTimeZone(DOMUtils.getUniqueElement(syncData, "TimeZone")));
		
		setEventCalendar(calendar, syncData);
		
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
				
				switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(syncData, "AttendeeStatus"))) {
				case 0: attendee.setAttendeeStatus(AttendeeStatus.RESPONSE_UNKOWN); break;
				case 2: attendee.setAttendeeStatus(AttendeeStatus.TENTATIVE); break;
				case 3: attendee.setAttendeeStatus(AttendeeStatus.ACCEPT); break;
				case 4: attendee.setAttendeeStatus(AttendeeStatus.DECLINE); break;
				case 5: attendee.setAttendeeStatus(AttendeeStatus.NOT_RESPONDED); break;
				}

				switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(syncData, "AttendeeStatus"))) {
				case 1: attendee.setAttendeeType(AttendeeType.REQUIRED); break;
				case 2: attendee.setAttendeeType(AttendeeType.OPTIONAL); break;
				case 3: attendee.setAttendeeType(AttendeeType.RESOURCE); break;
				}
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

				setEventCalendar(exception, subnode);
				
				exception.setExceptionStartTime(parseDOMDate(DOMUtils.getUniqueElement(subnode, "ExceptionStartTime")));
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
			recurrence.setOccurrences(parseDOMInt(DOMUtils.getUniqueElement(containerNode, "Occurrences")));
			recurrence.setInterval(parseDOMInt(DOMUtils.getUniqueElement(containerNode, "Interval")));
			recurrence.setDayOfWeek(parseDOMInt(DOMUtils.getUniqueElement(containerNode, "DayOfWeek")));
			calendar.setRecurrence(recurrence);
		}
		
		return null;
	}

	void setEventCalendar (IEventCalendar calendar, Element domSource) {
		calendar.setLocation(parseDOMString(DOMUtils.getUniqueElement(domSource, "Location")));
		calendar.setDtStamp(parseDOMDate(DOMUtils.getUniqueElement(domSource, "DtStamp")));
		calendar.setSubject(parseDOMString(DOMUtils.getUniqueElement(domSource, "Subject")));
		calendar.setEndTime(parseDOMDate(DOMUtils.getUniqueElement(domSource, "EndTime")));
		calendar.setStartTime(parseDOMDate(DOMUtils.getUniqueElement(domSource, "StartTime")));
		calendar.setAllDayEvent(parseDOMInt2Boolean(DOMUtils.getUniqueElement(domSource, "AllDayEvent")));
		calendar.setReminder(parseDOMInt(DOMUtils.getUniqueElement(domSource, "Reminder")));
		calendar.setCategories(parseDOMStringCollection(DOMUtils.getUniqueElement(domSource, "Categories"), "Category"));
		
		logger.info("alldayevent: "+(parseDOMInt2Boolean(DOMUtils.getUniqueElement(domSource, "AllDayEvent"))));
		
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource, "BusyStatus"))) {
		case 0: calendar.setBusyStatus(CalendarBusyStatus.FREE); break;
		case 1: calendar.setBusyStatus(CalendarBusyStatus.TENTATIVE); break;
		case 2: calendar.setBusyStatus(CalendarBusyStatus.BUSY); break;
		case 3: calendar.setBusyStatus(CalendarBusyStatus.OUT_OF_OFFICE); break;
		}

		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource, "Sensitivity"))) {
		case 0: calendar.setSensitivity(CalendarSensitivity.NORMAL); break;
		case 1: calendar.setSensitivity(CalendarSensitivity.PERSONAL); break;
		case 2: calendar.setSensitivity(CalendarSensitivity.PRIVATE); break;
		case 3: calendar.setSensitivity(CalendarSensitivity.CONFIDENTIAL); break;
		}
		
		switch (parseDOMNoNullInt(DOMUtils.getUniqueElement(domSource, "MeetingStatus"))) {
		case 0: calendar.setMeetingStatus(CalendarMeetingStatus.IS_NOT_IN_MEETING); break;
		case 1: calendar.setMeetingStatus(CalendarMeetingStatus.IS_IN_MEETING); break;
		case 3: calendar.setMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED); break;
		case 5: calendar.setMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED); break;
		case 7: calendar.setMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED); break;
		}
	}
}
