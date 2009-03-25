package org.obm.push.data;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.MSEvent;
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
	public void encode(Element p, IApplicationData data) {

		// TODO Auto-generated method stub
		MSEvent ev = (MSEvent) data;

		Element tz = DOMUtils.createElement(p, "Calendar:TimeZone");
		// taken from exchange 2k7 : eastern greenland, gmt+0, no dst
		tz
				.setTextContent("xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==");
		e(p, "Calendar:AllDayEvent", "0");
		e(p, "Calendar:BusyStatus", CalendarBusyStatus.BUSY.asIntString());

		e(p, "Calendar:StartTime", sdf.format(ev.getStartTime()));
		e(p, "Calendar:EndTime", sdf.format(ev.getEndTime()));
		e(p, "Calendar:UID", ev.getUID());
		e(p, "Calendar:Subject", ev.getSubject());
		e(p, "Calendar:Location", ev.getLocation());
		e(p, "Calendar:MeetingStatus", CalendarMeetingStatus.IS_NOT_IN_MEETING
				.asIntString());

	}

	private void e(Element p, String name, String val) {
		if (val != null && val.length() > 0) {
			DOMUtils.createElementAndText(p, name, val);
		}
	}

}
