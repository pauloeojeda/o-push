package org.obm.push.tnefconverter.test;

import java.io.InputStream;

import javax.swing.text.BadLocationException;

import org.obm.push.tnefconverter.ScheduleMeeting.ScheduleMeeting;

import junit.framework.TestCase;

import net.freeutils.tnef.Message;
import net.freeutils.tnef.TNEFInputStream;

public class TnefTest extends TestCase {

	public void testExtract() throws BadLocationException{
		InputStream in = loadDataFile("excptRecur.tnef");
//		InputStream in = loadDataFile("recurMonthly.tnef");
//		InputStream in = loadDataFile("recurYearly.tnef");
//		InputStream in = loadDataFile("test.tnef");
		assertNotNull(in);
		
		try {
			TNEFInputStream tnef = new TNEFInputStream(in);
			Message tnefMsg = new Message(tnef);
			System.out.println(tnefMsg);
			ScheduleMeeting ics = new ScheduleMeeting(tnefMsg);
			System.out.println("Method "+ics.getMethod());
			System.out.println("UID: "+ics.getUID());
			System.out.println("Start: "+ics.getStartDate());
			System.out.println("End: "+ics.getEndDate());
			System.out.println("Response requested: "+ics.getResponseRequested());
			System.out.println("Description: "+ics.getDescription());
			System.out.println("Class: "+ics.getClazz());
			System.out.println("Location: "+ics.getLocation());
			System.out.println("AllDay: "+ics.isAllDay());
			
			System.out.println("IsReccuring: "+ics.isRecurring());
			System.out.println("RecurrenceType: "+ics.getOldRecurrenceType());
			System.out.println("Interval: "+ics.getInterval());
			System.out.println("ClientIntent: "+ics.getClientIntent());
//			System.out.println("Timezone: "+ics.getTimeZone());
//			System.out.println("Start recurrence: "+ics.getStartRecurrence());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"data/tnef/" + name);
	}
}
