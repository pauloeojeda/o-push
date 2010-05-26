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
import org.obm.push.data.calendarenum.CalendarBusyStatus;
import org.obm.push.data.calendarenum.CalendarSensitivity;
import org.obm.push.data.calendarenum.RecurrenceDayOfWeek;
import org.obm.push.data.calendarenum.RecurrenceType;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
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

	// @SuppressWarnings("unused")
	private static final Log logger = LogFactory.getLog(EventConverter.class);

	public MSEvent convertEvent(Event e) {
		MSEvent mse = new MSEvent();
		if (e.getTimeUpdate() != null) {
			mse.setDtStamp(e.getTimeUpdate());
		} else {
			mse.setDtStamp(new Date());
		}

		mse.setSubject(e.getTitle());
		mse.setDescription(e.getDescription());
		mse.setLocation(e.getLocation());
		mse.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		mse.setStartTime(e.getDate());
		mse.setExceptionStartTime(e.getRecurrenceId());

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		c.setTimeInMillis(e.getDate().getTime());
		c.add(Calendar.SECOND, e.getDuration());
		mse.setEndTime(c.getTime());

		for (Attendee at : e.getAttendees()) {
			mse.addAttendee(convertAttendee(at));
		}

		mse.setOrganizerName(e.getOwner());
		mse.setOrganizerEmail(e.getOwnerEmail());
		mse.setAllDayEvent(e.isAllday());

		mse.setRecurrence(getRecurrence(e.getRecurrence()));

		mse.setExceptions(getException(e.getRecurrence()));

		if (e.getAlert() != null && e.getAlert() > 0) {
			mse.setReminder(e.getAlert() / 60);
		}
		mse.setUID(e.getExtId());
		mse.setObmUID(e.getUid());
		mse.setBusyStatus(busyStatus(e.getOpacity()));
		return mse;
	}

	private List<MSEvent> getException(EventRecurrence recurrence) {
		List<MSEvent> ret = new LinkedList<MSEvent>();
		for (Date excp : recurrence.getExceptions()) {
			MSEvent e = new MSEvent();
			e.setDeleted(true);
			e.setExceptionStartTime(excp);
			e.setStartTime(excp);
			e.setDtStamp(new Date());
			ret.add(e);
		}

		for (Event excp : recurrence.getEventExceptions()) {
			MSEvent e = convertEvent(excp);
			ret.add(e);
		}
		return ret;
	}

	private CalendarBusyStatus busyStatus(EventOpacity opacity) {
		switch (opacity) {
		case TRANSPARENT:
			return CalendarBusyStatus.FREE;
		default:
			return CalendarBusyStatus.BUSY;
		}
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
			or.setDays(getDays(pr.getDayOfWeek()));
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
			or.setDays(getDays(pr.getDayOfWeek()));
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

	private String getDays(Set<RecurrenceDayOfWeek> dayOfWeek) {
		StringBuilder sb = new StringBuilder();
		if (dayOfWeek == null) {
			return "0000000";
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.SUNDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.MONDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.TUESDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.WEDNESDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.THURSDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.FRIDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.SATURDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		return sb.toString();
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

	public Event convertEvent(Event oldEvent, MSEvent data) {
		Event e = convertEventOne(oldEvent, null, data);
		e.setExtId(data.getUID());
		if (data.getRecurrence() != null) {
			EventRecurrence r = getRecurrence(data);
			e.setRecurrence(r);
			if (data.getExceptions() != null && !data.getExceptions().isEmpty()) {
				for (MSEvent excep : data.getExceptions()) {
					if (!excep.isDeletedException()) {
						Event obmEvent = convertEventOne(oldEvent, e, excep);
						r.addEventException(obmEvent);
					} else {
						r.addException(excep.getExceptionStartTime());
					}
				}
			}
		}

		return e;
	}

	// Exceptions.Exception.Body (section 2.2.3.9): This element is optional.
	// Exceptions.Exception.Categories (section 2.2.3.8): This element is
	// optional.

	private Event convertEventOne(Event oldEvent, Event parentEvent,
			MSEvent data) {
		Event e = new Event();
		if (parentEvent != null && parentEvent.getTitle() != null
				&& !parentEvent.getTitle().isEmpty()) {
			e.setTitle(parentEvent.getTitle());
		} else {
			e.setTitle(data.getSubject());
		}
		if (parentEvent != null && parentEvent.getDescription() != null
				&& !parentEvent.getDescription().isEmpty()) {
			e.setDescription(parentEvent.getDescription());
		} else {
			e.setDescription(data.getDescription());
		}
		e.setLocation(data.getLocation());
		e.setDate(data.getStartTime());
		int duration = (int) (data.getEndTime().getTime() - data.getStartTime()
				.getTime()) / 1000;
		e.setDuration(duration);
		e.setAllday(data.getAllDayEvent());
		e.setRecurrenceId(data.getExceptionStartTime());
		if (data.getReminder() != null && data.getReminder() > 0) {
			e.setAlert(data.getReminder() * 60);
		}

		if (data.getAttendees() == null || data.getAttendees().isEmpty()) {
			// copy parent attendees. CalendarBackend ensured parentEvent has
			// attendees.
			e.setAttendees(parentEvent.getAttendees());
		} else {
			for (MSAttendee at : data.getAttendees()) {
				e.addAttendee(convertAttendee(oldEvent, at));
			}
		}

		if (data.getBusyStatus() == null) {
			if (parentEvent != null) {
				e.setOpacity(parentEvent.getOpacity());
			}
		} else {
			e.setOpacity(opacity(data.getBusyStatus()));
		}

		if (data.getSensitivity() == null) {
			if (parentEvent != null) {
				e.setPrivacy(parentEvent.getPrivacy());
			}
		} else {
			e.setPrivacy(privacy(oldEvent, data.getSensitivity()));
		}

		return e;
	}

	private int privacy(Event oldEvent, CalendarSensitivity sensitivity) {
		if (sensitivity == null) {
			return oldEvent != null ? oldEvent.getPrivacy() : 0;
		}
		switch (sensitivity) {
		case CONFIDENTIAL:
		case PERSONAL:
		case PRIVATE:
			return 1;
		case NORMAL:
		default:
			return 0;
		}

	}

	private EventOpacity opacity(CalendarBusyStatus busyStatus) {
		switch (busyStatus) {
		case FREE:
			return EventOpacity.TRANSPARENT;
		default:
			return EventOpacity.OPAQUE;
		}
	}

	private Attendee convertAttendee(Event oldEvent, MSAttendee at) {
		ParticipationState oldState = ParticipationState.NEEDSACTION;
		if(oldEvent != null){
			for(Attendee oldAtt : oldEvent.getAttendees()){
				if(oldAtt.getEmail().equals(at.getEmail())){
					oldState = oldAtt.getState();
					break;
				}
			}
		}
		Attendee ret = new Attendee();
		ret.setEmail(at.getEmail());
		ret.setRequired(ParticipationRole.REQ);
		ret.setState(status(oldState,at.getAttendeeStatus()));
		logger.info("Add attendee " + ret.getEmail() + " "
				+ ret.getDisplayName() + " " + at.getAttendeeStatus());
		return ret;
	}

	private ParticipationState status(ParticipationState oldParticipationState, AttendeeStatus attendeeStatus) {
		if (attendeeStatus == null) {
			return oldParticipationState;
		}
		switch (attendeeStatus) {
		case DECLINE:
			return ParticipationState.DECLINED;
		case NOT_RESPONDED:
			return ParticipationState.NEEDSACTION;
		case RESPONSE_UNKNOWN:
			return ParticipationState.NEEDSACTION;
		case TENTATIVE:
			return ParticipationState.NEEDSACTION;
		default:
		case ACCEPT:
			return ParticipationState.ACCEPTED;
		}
	}
}
