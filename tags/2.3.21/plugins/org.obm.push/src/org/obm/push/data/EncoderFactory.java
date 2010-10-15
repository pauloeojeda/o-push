package org.obm.push.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IApplicationData;

public class EncoderFactory {
	
	protected Log logger = LogFactory.getLog(getClass());
	
	public IDataEncoder getEncoder(IApplicationData data, double protocolVersion) {
		if (data != null) {
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
		} else {
			logger.warn("TRY TO ENCODE NULL OBJECT");
			return null;
		}
	}

}
