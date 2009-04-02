package org.obm.push.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.MSAttendee;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.Recurrence;
import org.obm.push.data.calendarenum.CalendarBusyStatus;
import org.obm.push.data.calendarenum.CalendarMeetingStatus;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class CalendarEncoder implements IDataEncoder {

	private SimpleDateFormat sdf;

	public CalendarEncoder() {
		sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	// <TimeZone>xP///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==</TimeZone>
	// <AllDayEvent>0</AllDayEvent>
	// <BusyStatus>2</BusyStatus>
	// <DTStamp>20010101T000000Z</DTStamp>
	// <EndTime>20010101T000000Z</EndTime>
	// <Sensitivity>0</Sensitivity>
	// <StartTime>20010101T000000Z</StartTime>
	// <UID>74455CE0E49D486DBDBC7CB224C5212D00000000000000000000000000000000</UID>
	// <MeetingStatus>0</MeetingStatus>

	@Override
	public void encode(BackendSession bs, Element p, IApplicationData data) {

		// TODO Auto-generated method stub
		MSEvent ev = (MSEvent) data;

		Element tz = DOMUtils.createElement(p, "Calendar:TimeZone");
		// taken from exchange 2k7 : eastern greenland, gmt+0, no dst
		tz
				.setTextContent("xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==");
		e(p, "Calendar:AllDayEvent", "0");
		e(p, "Calendar:BusyStatus", CalendarBusyStatus.BUSY.asIntString());
		e(p, "Calendar:Sensitivity", "0");
		e(p, "Calendar:DTStamp", sdf.format(new Date()));

		e(p, "Calendar:StartTime", sdf.format(ev.getStartTime()));
		e(p, "Calendar:EndTime", sdf.format(ev.getEndTime()));
		e(p, "Calendar:UID", ev.getUID());
		e(p, "Calendar:Subject", ev.getSubject());
		e(p, "Calendar:Location", ev.getLocation());
		e(p, "Calendar:MeetingStatus", CalendarMeetingStatus.IS_NOT_IN_MEETING
				.asIntString());

		e(p, "Calendar:AllDayEvent", (ev.getAllDayEvent() ? "1" : "0"));

		e(p, "Calendar:OrganizerName", ev.getOrganizerName());
		// TODO OrganizerMail

		if (ev.getReminder() != null) {
			e(p, "Calendar:ReminderMinsBefore", ev.getReminder().toString());
		}

		if (ev.getRecurrence() != null) {
			Element r = DOMUtils.createElement(p, "Calendar:Recurrence");
			DOMUtils.createElementAndText(r, "Calendar:RecurrenceType", rec(ev)
					.getType().asIntString());
			if (rec(ev).getInterval() != null) {
				DOMUtils.createElementAndText(r, "Calendar:RecurrenceInterval",
						rec(ev).getInterval().toString());
			}
			if (rec(ev).getUntil() != null) {
				DOMUtils.createElementAndText(r, "Calendar:RecurrenceUntil",
						sdf.format(rec(ev).getUntil()));
			}

			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			switch (rec(ev).getType()) {
			case DAILY:
				break;
			case MONTHLY:
				cal.setTimeInMillis(ev.getStartTime().getTime());
				DOMUtils.createElementAndText(r,
						"Calendar:RecurrenceDayOfMonth", "2");
				break;
			case MONTHLY_NDAY:
				break;
			case WEEKLY:
				break;
			case YEARLY:
				cal.setTimeInMillis(ev.getStartTime().getTime());
				DOMUtils.createElementAndText(r,
						"Calendar:RecurrenceDayOfMonth", "2");
				DOMUtils.createElementAndText(r,
						"Calendar:RecurrenceMonthOfYear", ""
								+ (cal.get(Calendar.MONTH) + 1));
				break;
			case YEARLY_NDAY:
				break;

			}

		}

		DOMUtils.createElement(p, "Calendar:Compressed_RTF");

		if (bs.checkHint("hint.loadAttendees", true)) {
			Element at = DOMUtils.createElement(p, "Calendar:Attendees");
			for (MSAttendee ma : ev.getAttendees()) {
				Element ae = DOMUtils.createElement(at, "Calendar:Attendee");
				e(ae, "Calendar:AttendeeEmail", ma.getEmail());
				e(ae, "Calendar:AttendeeName", ma.getName());
				e(ae, "Calendar:AttendeeStatus", ma.getAttendeeStatus()
						.asIntString());
				e(ae, "Calendar:AttendeeType", ma.getAttendeeType()
						.asIntString());
			}
		}

	}

	private Recurrence rec(MSEvent ev) {
		return ev.getRecurrence();
	}

	private void e(Element p, String name, String val) {
		if (val != null && val.length() > 0) {
			DOMUtils.createElementAndText(p, name, val);
		}
	}

}
