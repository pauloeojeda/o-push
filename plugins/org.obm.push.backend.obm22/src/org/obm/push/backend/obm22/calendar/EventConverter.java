package org.obm.push.backend.obm22.calendar;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.MSEvent;
import org.obm.sync.calendar.Event;

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

		logger.info("start: " + cal.getStartTime() + " end: "
				+ cal.getEndTime());
		return cal;
	}

}
