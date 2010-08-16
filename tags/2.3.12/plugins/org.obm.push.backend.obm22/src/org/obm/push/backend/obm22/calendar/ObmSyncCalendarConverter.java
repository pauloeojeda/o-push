package org.obm.push.backend.obm22.calendar;

import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.MSAttendee;
import org.obm.sync.calendar.Event;

public interface ObmSyncCalendarConverter {
	
	Event convert(Event oldEvent, IApplicationData data,MSAttendee ownerAtt);
	Event convert(IApplicationData appliData);
	
	IApplicationData convert(Event event);
}
