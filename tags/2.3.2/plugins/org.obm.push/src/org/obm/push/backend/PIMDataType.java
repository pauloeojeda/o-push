package org.obm.push.backend;

public enum PIMDataType {

	EMAIL, CALENDAR, CONTACTS, TASKS;

	public String asXmlValue() {
		switch (this) {
		case CALENDAR:
			return "Calendar";
		case CONTACTS:
			return "Contacts";
		case TASKS:
			return "Tasks";
		case EMAIL:
		default:
			return "Email";
		}
	}

}
