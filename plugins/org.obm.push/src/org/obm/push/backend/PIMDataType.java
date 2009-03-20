package org.obm.push.backend;

public enum PIMDataType {

	EMAIL, CALENDAR, CONTACT, TASK;

	public String asXmlValue() {
		switch (this) {
		case CALENDAR:
			return "Calendar";
		case CONTACT:
			return "Contact";
		case TASK:
			return "Task";
		case EMAIL:
		default:
			return "Email";

		}
	}

}
