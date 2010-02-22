package org.obm.push.backend;

import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.data.calendarenum.AttendeeType;

public class MSAttendee {
	private String email;
	private String name;
	private AttendeeStatus attendeeStatus;
	private AttendeeType attendeeType;
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public AttendeeStatus getAttendeeStatus() {
		return attendeeStatus;
	}
	public void setAttendeeStatus(AttendeeStatus attendeeStatus) {
		this.attendeeStatus = attendeeStatus;
	}
	public AttendeeType getAttendeeType() {
		return attendeeType;
	}
	public void setAttendeeType(AttendeeType attendeeType) {
		this.attendeeType = attendeeType;
	}
}
