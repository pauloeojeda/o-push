package org.obm.push.data;

import org.obm.push.backend.IApplicationData;

public class EncoderFactory {

	public IDataEncoder getEncoder(IApplicationData data) {
		switch (data.getType()) {

		case CALENDAR:
			return new CalendarEncoder();

		case CONTACT:
			return new ContactEncoder();

		case TASK:
			return new TaskEncoder();

		case EMAIL:
		default:
			return new MailEncoder();
		}
	}

}
