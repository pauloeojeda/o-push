package org.obm.push.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.MSAttendee;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.Recurrence;
import org.obm.push.backend.SyncCollection;
import org.obm.push.data.calendarenum.CalendarMeetingStatus;
import org.obm.push.data.calendarenum.RecurrenceDayOfWeek;
import org.obm.push.data.email.Type;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class CalendarEncoder implements IDataEncoder {

	private SimpleDateFormat sdf;

	private static final Pattern hexa = Pattern.compile("[0-9a-fA-F]");

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
	public void encode(BackendSession bs, Element p, IApplicationData data,
			SyncCollection c, boolean isReponse) {

		MSEvent ev = (MSEvent) data;

		Element tz = DOMUtils.createElement(p, "Calendar:TimeZone");
		// taken from exchange 2k7 : eastern greenland, gmt+0, no dst
		tz
				.setTextContent("xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==");

		e(p, "Calendar:DTStamp", sdf.format(ev.getDtStamp() != null ? ev
				.getDtStamp() : new Date()));

		e(p, "Calendar:StartTime", sdf.format(ev.getStartTime()));
		e(p, "Calendar:Subject", ev.getSubject());

		if (hexa.matcher(ev.getUID()).matches()) {
			e(p, "Calendar:UID", ev.getUID());
		} else if (ev.getUID().hashCode() != 0) {
			e(p, "Calendar:UID", Integer.toHexString(ev.getUID().hashCode()));
		}
		if (ev.getOrganizerEmail() != null) {
			e(p, "Calendar:OrganizerName", ev.getOrganizerName());
			e(p, "Calendar:OrganizerEmail", ev.getOrganizerEmail());
		} else {
			// FIXME we need a proper name & email
			String l = bs.getLoginAtDomain();
			int idx = l.indexOf("@");
			if (idx > 0) {
				l = l.substring(0, idx);
			}
			e(p, "Calendar:OrganizerName", l);
			e(p, "Calendar:OrganizerEmail", bs.getLoginAtDomain());
		}

		if (bs.checkHint("hint.loadAttendees", true)) {
			Element at = DOMUtils.createElement(p, "Calendar:Attendees");
			if (ev.getAttendees().size() > 1) {
				for (MSAttendee ma : ev.getAttendees()) {
					Element ae = DOMUtils
							.createElement(at, "Calendar:Attendee");
					e(ae, "Calendar:AttendeeEmail", ma.getEmail());
					e(ae, "Calendar:AttendeeName", ma.getName());
					if (bs.getProtocolVersion() >= 12) {
						e(ae, "Calendar:AttendeeStatus", ma.getAttendeeStatus()
								.asIntString());
						e(ae, "Calendar:AttendeeType", ma.getAttendeeType()
								.asIntString());
					}
				}
			}
		}

		e(p, "Calendar:Location", ev.getLocation());
		e(p, "Calendar:EndTime", sdf.format(ev.getEndTime()));

		encodeBody(c, bs, p, ev);

		if (ev.getRecurrence() != null) {
			encodeRecurrence(p, ev);
			encodeExceptions(c, bs, p, ev.getExceptions());
		}

		e(p, "Calendar:Sensitivity", "0");
		e(p, "Calendar:BusyStatus", ev.getBusyStatus().asIntString());

		if (ev.getAllDayEvent()) {
			e(p, "Calendar:AllDayEvent", (ev.getAllDayEvent() ? "1" : "0"));
		} else {
			e(p, "Calendar:AllDayEvent", "0");
		}

		if (bs.checkHint("hint.loadAttendees", true)
				&& ev.getAttendees().size() > 1) {
			e(p, "Calendar:MeetingStatus", CalendarMeetingStatus.IS_IN_MEETING
					.asIntString());
		} else {
			e(p, "Calendar:MeetingStatus",
					CalendarMeetingStatus.IS_NOT_IN_MEETING.asIntString());
		}

		if (isReponse && bs.getProtocolVersion() > 12) {
			e(p, "AirSyncBase:NativeBodyType", Type.PLAIN_TEXT.toString());
		}

		if (ev.getReminder() != null) {
			e(p, "Calendar:ReminderMinsBefore", ev.getReminder().toString());
		}

		// DOMUtils.createElement(p, "Calendar:Compressed_RTF");

	}

	private void encodeExceptions(SyncCollection c, BackendSession bs,
			Element p, List<MSEvent> excepts) {
		// Exceptions.Exception
		Element es = DOMUtils.createElement(p, "Calendar:Exceptions");
		for (MSEvent ex : excepts) {
			Element e = DOMUtils.createElement(es, "Calendar:Exception");
			if (ex.isDeletedException()) {

				DOMUtils.createElementAndText(e, "Calendar:ExceptionIsDeleted",
						"1");
				DOMUtils
						.createElementAndText(e, "Calendar:MeetingStatus",
								CalendarMeetingStatus.MEETING_IS_CANCELED
										.asIntString());

			} else {
				if (bs.checkHint("hint.loadAttendees", true)
						&& ex.getAttendees().size() > 1) {
					e(e, "Calendar:MeetingStatus",
							CalendarMeetingStatus.IS_IN_MEETING.asIntString());
				} else {
					e(e, "Calendar:MeetingStatus",
							CalendarMeetingStatus.IS_NOT_IN_MEETING
									.asIntString());
				}

				encodeBody(c, bs, e, ex);

				e(e, "Calendar:Location", ex.getLocation());
				e(e, "Calendar:Sensitivity", "0");
				e(e, "Calendar:BusyStatus", ex.getBusyStatus().asIntString());
				e(e, "Calendar:AllDayEvent", (ex.getAllDayEvent() ? "1" : "0"));
				if (ex.getReminder() != null) {
					e(e, "Calendar:ReminderMinsBefore", ex.getReminder()
							.toString());
				}
				DOMUtils.createElement(e, "Calendar:Categories");
			}
			e(e, "Calendar:Subject", ex.getSubject());

			DOMUtils.createElementAndText(e, "Calendar:ExceptionStartTime", sdf
					.format(ex.getExceptionStartTime()));

			DOMUtils.createElementAndText(e, "Calendar:StartTime", sdf
					.format(ex.getStartTime()));
			if (ex.getEndTime() != null) {
				DOMUtils.createElementAndText(e, "Calendar:EndTime", sdf
						.format(ex.getEndTime()));
			}
			if (ex.getDtStamp() != null) {
				DOMUtils.createElementAndText(e, "Calendar:DTStamp", sdf
						.format(ex.getDtStamp()));
			}
		}
	}

	private void encodeBody(SyncCollection c, BackendSession bs, Element p,
			MSEvent event) {
		String body = "";
		if (event.getDescription() != null) {
			body = event.getDescription().trim();
		}
		if (bs.getProtocolVersion() >= 12) {
			Element d = DOMUtils.createElement(p, "AirSyncBase:Body");
			e(d, "AirSyncBase:Type", Type.PLAIN_TEXT.toString());
			e(d, "AirSyncBase:EstimatedDataSize", "" + body.length());
			if (body.length() > 0) {
				DOMUtils.createElementAndText(d, "AirSyncBase:Data", body);
			}
		} else {
			if (event.getDescription() != null
					&& event.getDescription().length() > 0) {
				// Doesn't work with wm5
				// DOMUtils.createElementAndText(p, "Email:BodyTruncated", "0");
				// DOMUtils.createElementAndText(p, "Email:Body",
				// event.getDescription()+"\r\n");
			}
		}
	}

	private void encodeRecurrence(Element p, MSEvent ev) {
		Element r = DOMUtils.createElement(p, "Calendar:Recurrence");
		DOMUtils.createElementAndText(r, "Calendar:RecurrenceType", rec(ev)
				.getType().asIntString());
		if (rec(ev).getInterval() != null) {
			DOMUtils.createElementAndText(r, "Calendar:RecurrenceInterval",
					rec(ev).getInterval().toString());
		}
		if (rec(ev).getUntil() != null) {
			DOMUtils.createElementAndText(r, "Calendar:RecurrenceUntil", sdf
					.format(rec(ev).getUntil()));
		}

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(ev.getStartTime().getTime());
		switch (rec(ev).getType()) {
		case DAILY:
			break;
		case MONTHLY:

			DOMUtils.createElementAndText(r, "Calendar:RecurrenceDayOfMonth",
					"" + cal.get(Calendar.DAY_OF_MONTH));
			break;
		case MONTHLY_NDAY:
			DOMUtils.createElementAndText(r, "Calendar:RecurrenceWeekOfMonth",
					"" + cal.get(Calendar.WEEK_OF_MONTH));
			DOMUtils.createElementAndText(r, "Calendar:RecurrenceDayOfWeek", ""
					+ RecurrenceDayOfWeek.dayOfWeekToInt(cal
							.get(Calendar.DAY_OF_WEEK)));
			break;
		case WEEKLY:
			DOMUtils.createElementAndText(r, "Calendar:RecurrenceDayOfWeek", ""
					+ RecurrenceDayOfWeek.asInt(rec(ev).getDayOfWeek()));
			break;
		case YEARLY:
			DOMUtils.createElementAndText(r, "Calendar:RecurrenceDayOfMonth",
					"" + cal.get(Calendar.DAY_OF_MONTH));
			DOMUtils.createElementAndText(r, "Calendar:RecurrenceMonthOfYear",
					"" + (cal.get(Calendar.MONTH) + 1));
			break;
		case YEARLY_NDAY:
			break;
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
