package org.obm.push.tnefconverter.ScheduleMeeting;

import java.net.URISyntaxException;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.columba.ristretto.message.Address;
import org.obm.push.tnefconverter.helper.ICSHelper;

/**
 * 
 * @author adrienp
 * 
 */
public class ScheduleMeetingEncoder {

	private Log logger = LogFactory.getLog(getClass());

	private ScheduleMeeting meeting;
	private Calendar ics;
	private VEvent vEvent;
	private String title;
	private Address owner;
	private Set<Address> attRequired;
	private Set<Address> attOptional;

	public ScheduleMeetingEncoder(ScheduleMeeting meeting, String title,
			Address owner, Set<Address> attRequired, Set<Address> attOptional) {
		this.meeting = meeting;
		this.title = title;
		this.owner = owner;
		this.attRequired = attRequired;
		this.attOptional = attOptional;
	}

	public String encodeToIcs() throws Exception {
		ics = ICSHelper.initCalendar();
		appendMethod();
		vEvent = new VEvent();
		ics.getComponents().add(vEvent);
		appendUID();
		appendSummary();
		appendLocation();
		appendDescription();
		appendClass();
		appendDtStart();
		appendDtEnd();
		appendOrganizer();
		appendAttendee();
		appendRRule();
		return ics.toString();
	}

	//TODO IMPLEMENTS MONTHLY_NDAY and YEARLY_NDAY (recur.setWeekStartDay(weekStartDay))
	private void appendRRule() {

		if (meeting.isRecurring() != null) {
			String frequency = "";
			OldRecurrenceType kindRecur = meeting.getOldRecurrenceType();
			if (OldRecurrenceType.DAILY.equals(kindRecur)) {
				frequency = Recur.DAILY;
			} else if (OldRecurrenceType.WEEKLY.equals(kindRecur)) {
				frequency = Recur.WEEKLY;
			} else if (OldRecurrenceType.MONTHLY.equals(kindRecur)) {
				frequency = Recur.MONTHLY;
			} else if (OldRecurrenceType.MONTHLY.equals(kindRecur) || OldRecurrenceType.MONTHLY_NDAY.equals(kindRecur)) {
				frequency = Recur.MONTHLY;
			} else if (OldRecurrenceType.YEARLY.equals(kindRecur) || OldRecurrenceType.YEARLY_NDAY.equals(kindRecur)) {
				frequency = Recur.YEARLY;
			} else {
				frequency = "";
			}
			
			Recur recur = new Recur(frequency,null);
			recur.setInterval(meeting.getInterval());
			RRule rrule = new RRule(recur);
			vEvent.getProperties().add(rrule);
		}
	}

	private void appendAttendee() {
		for (Address add : attRequired) {
			Attendee att = createAttendee(add, Role.REQ_PARTICIPANT);
			vEvent.getProperties().add(att);
		}

		for (Address add : attOptional) {
			Attendee att = createAttendee(add, Role.OPT_PARTICIPANT);
			vEvent.getProperties().add(att);
		}
	}

	private Attendee createAttendee(Address add, Role role) {
		Attendee att = new Attendee();
		att.getParameters().add(CuType.INDIVIDUAL);
		att.getParameters().add(PartStat.NEEDS_ACTION);
		if (isNotEmpty(add.getDisplayName())) {
			att.getParameters().add(new Cn(add.getDisplayName()));
		} else {
			att.getParameters().add(new Cn(add.getMailAddress()));
		}
		att.getParameters().add(role);
		try {
			att.setValue("mailto:" + add.getMailAddress());
		} catch (URISyntaxException e) {
			logger.error("Error while parsing mail address "
					+ add.getMailAddress());
		}
		return att;
	}

	private void appendOrganizer() {
		if (owner != null) {
			Organizer orga = new Organizer();
			try {
				if (isNotEmpty(owner.getDisplayName())) {
					orga.getParameters().add(new Cn(owner.getDisplayName()));
				}
				if (isNotEmpty(owner.getMailAddress())) {
					orga.setValue("mailto:" + owner.getMailAddress());
				}
				vEvent.getProperties().add(orga);
			} catch (URISyntaxException e) {
				logger.error("Error while parsing mail address "
						+ owner.getMailAddress());
			}
		}
	}

	private void appendDtStart() {
		if (meeting.getStartDate() != null) {
			net.fortuna.ical4j.model.Date dt = null;
			if (meeting.isAllDay()) {
				dt = new net.fortuna.ical4j.model.Date(meeting.getStartDate()
						.getTime() + 43200000);
			} else {
				dt = new DateTime(meeting.getStartDate());
			}
			vEvent.getProperties().add(new DtStart(dt));
		}
	}
	
	private void appendDtEnd() {
		if (!meeting.isAllDay()) {
			DtEnd dtEnd = new DtEnd(new DateTime(meeting.getEndDate()));
			if (dtEnd != null) {
				vEvent.getProperties().add(dtEnd);
			}
		}
	}

	private void appendClass() {
		if (meeting.getClazz().equals(1)) {
			vEvent.getProperties().add(Clazz.PRIVATE);
		} else {
			vEvent.getProperties().add(Clazz.PUBLIC);
		}
	}

	private void appendDescription() {
		if(isNotEmpty(meeting.getDescription())){
			vEvent.getProperties().add(new Description(meeting.getDescription()));
		}
	}

	private void appendLocation() {
		if(isNotEmpty(meeting.getLocation())){
			vEvent.getProperties().add(new Location(meeting.getLocation()));
		}
	}

	private void appendSummary() {
		if(isNotEmpty(this.title)){
			vEvent.getProperties().add(new Summary(this.title.trim()));
		}
	}

	private void appendMethod() {
		PidTagMessageClass method = meeting.getMethod();
		switch (method) {
		case ScheduleMeetingRequest:
			ics.getProperties().add(Method.REQUEST);
			break;
		case ScheduleMeetingCanceled:
			ics.getProperties().add(Method.CANCEL);
			break;
		}
	}

	private void appendUID() throws Exception {
		vEvent.getProperties().add(new Uid(meeting.getUID()));
	}

	private boolean isNotEmpty(String val) {
		return val != null && !"".equals(val);
	}
}
