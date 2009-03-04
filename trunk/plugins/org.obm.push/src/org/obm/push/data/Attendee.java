package org.obm.push.data;

public class Attendee {
	private String email;
	private String name;
	private Byte attendeeStatus;
	private Byte attendeeType;
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
	public Byte getAttendeeStatus() {
		return attendeeStatus;
	}
	public void setAttendeeStatus(Byte attendeeStatus) {
		this.attendeeStatus = attendeeStatus;
	}
	public Byte getAttendeeType() {
		return attendeeType;
	}
	public void setAttendeeType(Byte attendeeType) {
		this.attendeeType = attendeeType;
	}
}
