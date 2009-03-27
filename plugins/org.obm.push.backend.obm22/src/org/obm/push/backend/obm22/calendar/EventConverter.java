package org.obm.push.backend.obm22.calendar;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.MSAttendee;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.Recurrence;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.data.calendarenum.AttendeeType;
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

	@SuppressWarnings("unused")
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

		return cal;
	}

	private Recurrence getRecurrence(EventRecurrence recurrence) {
		if (recurrence.getKind() == RecurrenceKind.none) {
			return null;
		}

		Recurrence r = new Recurrence();
		switch (recurrence.getKind()) {
		case daily:
			r.setType(RecurrenceType.DAILY);
		case monthlybydate:
			r.setType(RecurrenceType.MONTHLY);
		case monthlybyday:
			r.setType(RecurrenceType.MONTHLY_NDAY);
		case weekly:
			r.setType(RecurrenceType.WEEKLY);
		case yearly:
			r.setType(RecurrenceType.YEARLY);
		}
		r.setUntil(recurrence.getEnd());
		
		r.setInterval(recurrence.getFrequence());
		
		// TODO Auto-generated method stub
		return r;
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
		Event e = new Event();
		// TODO Auto-generated method stub
		e.setTitle(data.getSubject());
		e.setLocation(data.getLocation());
		e.setDate(data.getStartTime());
		int duration =(int) (data.getEndTime().getTime() - data.getStartTime().getTime()) / 1000; 
		e.setDuration(duration);
		e.setAllday(data.getAllDayEvent());
		
		return e;
	}

}