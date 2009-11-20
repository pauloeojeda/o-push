package org.obm.push.data;

import org.obm.push.backend.IApplicationData;

public class EncoderFactory {

	public IDataEncoder getEncoder(IApplicationData data) {
		switch (data.getType()) {

		case CALENDAR:
			return new CalendarEncoder();

		case CONTACTS:
			return new ContactEncoder();

		case TASKS:
			return new TaskEncoder();

		case EMAIL:
		default:
			return new EmailEncoder();
		}
	}

}
