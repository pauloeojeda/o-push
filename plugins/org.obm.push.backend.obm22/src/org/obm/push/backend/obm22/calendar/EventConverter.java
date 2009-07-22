package org.obm.push.backend.obm22.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.MSAttendee;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.Recurrence;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.data.calendarenum.AttendeeType;
import org.obm.push.data.calendarenum.RecurrenceDayOfWeek;
import org.obm.push.data.calendarenum.RecurrenceType;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

/**
 * Convert events between OBM-Sync object model & Microsoft object model
 * 
 * @author tom
 * 
 */
public class EventConverter {

	private static final Log logger = LogFactory.getLog(EventConverter.class);

	public MSEvent convertEvent(Event e) {
		MSEvent cal = new MSEvent();

		cal.setSubject(e.getTitle());
		cal.setLocation(e.getLocation());
		cal.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		cal.setStartTime(e.getDate());

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		c.setTimeInMillis(e.getDate().getTime());
		c.add(Calendar.SECOND, e.getDuration());
		cal.setEndTime(c.getTime());

		List<MSAttendee> l = new LinkedList<MSAttendee>();
		for (Attendee at : e.getAttendees()) {
			l.add(convertAttendee(at));
		}
		cal.setAttendees(l);

		cal.setOrganizerName(e.getOwner());
		cal.setAllDayEvent(e.isAllday());

		cal.setRecurrence(getRecurrence(e.getRecurrence()));

		if (e.getAlert() != null && e.getAlert() > 0) {
			cal.setReminder(e.getAlert());
		}
		cal.setUID(e.getExtId());
		return cal;
	}

	private EventRecurrence getRecurrence(MSEvent msev) {
		Date startDate = msev.getStartTime();
		Recurrence pr = msev.getRecurrence();
		EventRecurrence or = new EventRecurrence();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		int multiply = 0;
		switch (pr.getType()) {
		case DAILY:
			or.setKind(RecurrenceKind.daily);
			multiply = Calendar.DAY_OF_MONTH;
			break;
		case MONTHLY:
			or.setKind(RecurrenceKind.monthlybydate);
			multiply = Calendar.MONTH;
			break;
		case MONTHLY_NDAY:
			or.setKind(RecurrenceKind.monthlybyday);
			multiply = Calendar.MONTH;
			break;
		case WEEKLY:
			or.setKind(RecurrenceKind.weekly);
			multiply = Calendar.WEEK_OF_YEAR;
			break;
		case YEARLY:
			or.setKind(RecurrenceKind.yearly);
			cal.setTimeInMillis(startDate.getTime());
			cal.set(Calendar.DAY_OF_MONTH, pr.getDayOfMonth());
			cal.set(Calendar.MONTH, pr.getMonthOfYear() - 1);
			msev.setStartTime(cal.getTime());
			or.setFrequence(1);
			multiply = Calendar.YEAR;
			break;
		case YEARLY_NDAY:
			or.setKind(RecurrenceKind.yearly);
			multiply = Calendar.YEAR;
			break;
		}

		// interval
		if (pr.getInterval() != null) {
			or.setFrequence(pr.getInterval());
		}

		// occurence or end date
		Date endDate = null;
		if (pr.getOccurrences() != null && pr.getOccurrences() > 0) {
			cal.setTimeInMillis(startDate.getTime());
			cal.add(multiply, pr.getOccurrences() - 1);
			endDate = new Date(cal.getTimeInMillis());
		} else {
			endDate = pr.getUntil();
		}
		or.setEnd(endDate);

		return or;
	}

	private Recurrence getRecurrence(EventRecurrence recurrence) {
		if (recurrence.getKind() == RecurrenceKind.none) {
			return null;
		}

		Recurrence r = new Recurrence();
		switch (recurrence.getKind()) {
		case daily:
			r.setType(RecurrenceType.DAILY);
			break;
		case monthlybydate:
			r.setType(RecurrenceType.MONTHLY);
			break;
		case monthlybyday:
			r.setType(RecurrenceType.MONTHLY_NDAY);
			break;
		case weekly:
			r.setType(RecurrenceType.WEEKLY);
			r.setDayOfWeek(daysOfWeek(recurrence.getDays()));
			break;
		case yearly:
			r.setType(RecurrenceType.YEARLY);
			break;
		}
		r.setUntil(recurrence.getEnd());

		r.setInterval(recurrence.getFrequence());

		// TODO Auto-generated method stub
		return r;
	}

	private Set<RecurrenceDayOfWeek> daysOfWeek(String string) {
		char[] days = string.toCharArray();
		Set<RecurrenceDayOfWeek> daysList = new HashSet<RecurrenceDayOfWeek>();
		int i = 0;

		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.SUNDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.MONDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.TUESDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.WEDNESDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.THURSDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.FRIDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.SATURDAY);
		}

		return daysList;
	}

	private MSAttendee convertAttendee(Attendee at) {
		MSAttendee msa = new MSAttendee();

		msa.setAttendeeStatus(status(at.getState()));
		msa.setEmail(at.getEmail());
		msa.setName(at.getDisplayName());
		msa.setAttendeeType(type(at.getRequired()));

		return msa;
	}

	private AttendeeType type(ParticipationRole required) {
		// TODO Auto-generated method stub
		return AttendeeType.REQUIRED;
	}

	private AttendeeStatus status(ParticipationState state) {
		switch (state) {
		case COMPLETED:
			return AttendeeStatus.RESPONSE_UNKNOWN;
		case DECLINED:
			return AttendeeStatus.DECLINE;
		case DELEGATED:
			return AttendeeStatus.RESPONSE_UNKNOWN;
		case INPROGRESS:
			return AttendeeStatus.NOT_RESPONDED;
		case NEEDSACTION:
			return AttendeeStatus.NOT_RESPONDED;
		case TENTATIVE:
			return AttendeeStatus.TENTATIVE;

		default:
		case ACCEPTED:
			return AttendeeStatus.ACCEPT;

		}

	}

	public Event convertEvent(MSEvent data) {
		Event e = convertEventOne(null, data);
		e.setExtId(data.getUID());

		if (data.getRecurrence() != null) {
			EventRecurrence r = getRecurrence(data);
			e.setRecurrence(r);
			if (data.getExceptions() != null && !data.getExceptions().isEmpty()) {
				for (MSEvent excep : data.getExceptions()) {
					if (!excep.isDeletedException()) {
						Event obmEvent = convertEventOne(e, excep);
						r.addEventException(obmEvent);
						logger.info("changed occurence for event "
								+ data.getSubject() + " (" + excep.getSubject()
								+ ") " + excep.getExceptionStartTime());
					}
					r.addException(excep.getExceptionStartTime());
				}
			} else {
				logger.info(data.getSubject()
						+ " repeating event without exceptions.");
			}
		}

		return e;
	}

	private Event convertEventOne(Event parentEvent, MSEvent data) {
		Event e = new Event();
		e.setTitle(data.getSubject());
		e.setLocation(data.getLocation());
		e.setDate(data.getStartTime());
		int duration = (int) (data.getEndTime().getTime() - data.getStartTime()
				.getTime()) / 1000;
		e.setDuration(duration);
		e.setAllday(data.getAllDayEvent());

		if (data.getReminder() != null && data.getReminder() > 0) {
			e.setAlert(data.getReminder());
		}

		return e;
	}
}
