package org.obm.push.backend.obm22.calendar;

import java.util.TimeZone;

import org.obm.push.backend.MSEvent;
import org.obm.sync.calendar.Event;

public class EventConverter {

	public MSEvent convertEvent(Event e) {
		MSEvent cal = new MSEvent();

		cal.setSubject(e.getTitle());
		cal.setLocation(e.getLocation());
		cal.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		cal.setStartTime(e.getDate());
		
		
		return cal;
	}

}
