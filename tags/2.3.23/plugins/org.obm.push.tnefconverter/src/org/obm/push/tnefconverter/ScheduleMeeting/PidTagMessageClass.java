package org.obm.push.tnefconverter.ScheduleMeeting;

/**
 * 
 * @author adrienp
 * 
 */
public enum PidTagMessageClass {
	ScheduleMeetingRequest, ScheduleMeetingCanceled, ScheduleMeetingRespPos, ScheduleMeetingRespTent, ScheduleMeetingRespNeg;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	public String toString() {
		switch (this) {
		case ScheduleMeetingRequest:
			return "IPM.Microsoft Schedule.MtgReq";
		case ScheduleMeetingCanceled:
			return "IPM.Microsoft Schedule.MtgCncl";
		case ScheduleMeetingRespPos:
			return "IPM.Microsoft Schedule.MtgRespP";
		case ScheduleMeetingRespTent:
			return "IPM.Microsoft Schedule.MtgRespA";
		case ScheduleMeetingRespNeg:
			return "IPM.Microsoft Schedule.MtgRespN";
		default:
			return null;
		}
	}
	
	public static PidTagMessageClass getPidTagMessageClass(String val) {
		if ("IPM.Microsoft Schedule.MtgReq".equals(val)) {
			return ScheduleMeetingRequest;
		} else if ("IPM.Microsoft Schedule.MtgCncl".equals(val)) {
			return ScheduleMeetingCanceled;
		} else if ("IPM.Microsoft Schedule.MtgRespP".equals(val)) {
			return ScheduleMeetingRespPos;
		} else if ("IPM.Microsoft Schedule.MtgRespA".equals(val)) {
			return ScheduleMeetingRespTent;
		} else if ("IPM.Microsoft Schedule.MtgRespN".equals(val)) {
			return ScheduleMeetingRespNeg;
		} else {
			return null;
		}
	}
}
