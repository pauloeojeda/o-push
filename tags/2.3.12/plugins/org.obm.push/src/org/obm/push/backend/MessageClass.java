package org.obm.push.backend;

/**
 * 
 * @author adrienp
 *
 */
public enum MessageClass {
	Note, NoteRulesOofTemplateMicrosoft, NoteSMIME, NoteSMIMEMultipartSigned, ScheduleMeetingRequest, ScheduleMeetingCanceled, ScheduleMeetingRespPos, ScheduleMeetingRespTent, ScheduleMeetingRespNeg, Post;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString() {
		switch (this) {
		case Note:
			return "IPM.Note";
		case NoteRulesOofTemplateMicrosoft:
			return "IPM.Note.Rules.OofTemplate.Microsoft";
		case NoteSMIME:
			return "IPM.Note.SMIME";
		case NoteSMIMEMultipartSigned:
			return "IPM.Note.SMIME.MultipartSigned";
		case ScheduleMeetingRequest:
			return "IPM.Schedule.Meeting.Request";
		case ScheduleMeetingCanceled:
			return "IPM.Schedule.Meeting.Canceled";
		case ScheduleMeetingRespPos:
			return "IPM.Schedule.Meeting.Resp.Pos";
		case ScheduleMeetingRespTent:
			return "IPM.Schedule.Meeting.Resp.Tent";
		case ScheduleMeetingRespNeg:
			return "IPM.Schedule.Meeting.Resp.Neg";
		case Post:
			return "IPM.Post";
		default:
			return "IPM.Note";
		}
	}

}
