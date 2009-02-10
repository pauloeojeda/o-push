package org.obm.push.wbxml;

import java.util.HashMap;
import java.util.Map;

public class TagsTables {

	public static final String[] CP_0 = { "Synchronize", "Replies", "Add",
			"Modify", "Remove", "Fetch", "SyncKey", "ClientEntryId",
			"ServerEntryId", "Status", "Folder", "FolderType", "Version",
			"FolderId", "GetChanges", "MoreAvailable", "MaxItems", "Perform",
			"Options", "FilterType", "Truncation", "RtfTruncation", "Conflict",
			"Folders", "Data", "DeletesAsMoves", "NotifyGUID", "Supported",
			"SoftDelete", "MIMESupport", "MIMETruncation", };
	public static final String[] CP_1 = { "Anniversary", "AssistantName",
			"AssistnamePhoneNumber", "Birthday", "Body", "BodySize",
			"BodyTruncated", "Business2PhoneNumber", "BusinessCity",
			"BusinessCountry", "BusinessPostalCode", "BusinessState",
			"BusinessStreet", "BusinessFaxNumber", "BusinessPhoneNumber",
			"CarPhoneNumber", "Categories", "Category", "Children", "Child",
			"CompanyName", "Department", "Email1Address", "Email2Address",
			"Email3Address", "FileAs", "FirstName", "Home2PhoneNumber",
			"HomeCity", "HomeCountry", "HomePostalCode", "HomeState",
			"HomeStreet", "HomeFaxNumber", "HomePhoneNumber", "JobTitle",
			"LastName", "MiddleName", "MobilePhoneNumber", "OfficeLocation",
			"OtherCity", "OtherCountry", "OtherPostalCode", "OtherState",
			"OtherStreet", "PagerNumber", "RadioPhoneNumber", "Spouse",
			"Suffix", "Title", "WebPage", "YomiCompanyName", "YomiFirstName",
			"YomiLastName", "Rtf", "Picture", };
	public static final String[] CP_2 = { "Attachment", "Attachments",
			"AttName", "AttSize", "AttOid", "AttMethod", "AttRemoved", "Body",
			"BodySize", "BodyTruncated", "DateReceived", "DisplayName",
			"DisplayTo", "Importance", "MessageClass", "Subject", "Read", "To",
			"Cc", "From", "Reply-To", "AllDayEvent", "Categories", "Category",
			"DtStamp", "EndTime", "InstanceType", "BusyStatus", "Location",
			"MeetingRequest", "Organizer", "RecurrenceId", "Reminder",
			"ResponseRequested", "Recurrences", "Recurrence", "Type", "Until",
			"Occurrences", "Interval", "DayOfWeek", "DayOfMonth",
			"WeekOfMonth", "MonthOfYear", "StartTime", "Sensitivity",
			"TimeZone", "GlobalObjId", "ThreadTopic", "MIMEData",
			"MIMETruncated", "MIMESize", "InternetCPID", };
	public static final String[] CP_3 = { "Notify", "Notification", "Version",
			"Lifetime", "DeviceInfo", "Enable", "Folder", "ServerEntryId",
			"DeviceAddress", "ValidCarrierProfiles", "CarrierProfile",
			"Status", "Replies",
			// "Version='1.1'",
			"Devices", "Device", "Id", "Expiry", "NotifyGUID", };
	public static final String[] CP_4 = { "Timezone", "AllDayEvent",
			"Attendees", "Attendee", "Email", "Name", "Body", "BodyTruncated",
			"BusyStatus", "Categories", "Category", "Rtf", "DtStamp",
			"EndTime", "Exception", "Exceptions", "Deleted",
			"ExceptionStartTime", "Location", "MeetingStatus",
			"OrganizerEmail", "OrganizerName", "Recurrence", "Type", "Until",
			"Occurrences", "Interval", "DayOfWeek", "DayOfMonth",
			"WeekOfMonth", "MonthOfYear", "Reminder", "Sensitivity", "Subject",
			"StartTime", "UID", };
	public static final String[] CP_5 = { "Moves", "Move", "SrcMsgId",
			"SrcFldId", "DstFldId", "Response", "Status", "DstMsgId", };
	public static final String[] CP_6 = { "GetItemEstimate", "Version",
			"Folders", "Folder", "FolderType", "FolderId", "DateTime",
			"Estimate", "Response", "Status", };
	public static final String[] CP_7 = { "Folders", "Folder", "DisplayName",
			"ServerEntryId", "ParentId", "Type", "Response", "Status",
			"ContentClass", "Changes", "Add", "Remove", "Update", "SyncKey",
			"FolderCreate", "FolderDelete", "FolderUpdate", "FolderSync",
			"Count", "Version", };
	public static final String[] CP_8 = { "CalendarId", "FolderId",
			"MeetingResponse", "RequestId", "Request", "Result", "Status",
			"UserResponse", "Version", };
	public static final String[] CP_9 = { "Body", "BodySize", "BodyTruncated",
			"Categories", "Category", "Complete", "DateCompleted", "DueDate",
			"UtcDueDate", "Importance", "Recurrence", "Type", "Start", "Until",
			"Occurrences", "Interval", "DayOfWeek", "DayOfMonth",
			"WeekOfMonth", "MonthOfYear", "Regenerate", "DeadOccur",
			"ReminderSet", "ReminderTime", "Sensitivity", "StartDate",
			"UtcStartDate", "Subject", "Rtf", };
	public static final String[] CP_10 = { "ResolveRecipients", "Response",
			"Status", "Type", "Recipient", "DisplayName", "EmailAddress",
			"Certificates", "Certificate", "MiniCertificate", "Options", "To",
			"CertificateRetrieval", "RecipientCount", "MaxCertificates",
			"MaxAmbiguousRecipients", "CertificateCount", };
	public static final String[] CP_11 = { "ValidateCert", "Certificates",
			"Certificate", "CertificateChain", "CheckCRL", "Status", };
	public static final String[] CP_12 = { "CustomerId", "GovernmentId",
			"IMAddress", "IMAddress2", "IMAddress3", "ManagerName",
			"CompanyMainPhone", "AccountName", "NickName", "MMS", };
	public static final String[] CP_13 = { "Ping", "Status", "LifeTime",
			"Folders", "Folder", "ServerEntryId", "FolderType", };

	public static final Map<String, Integer> NAMESPACES_IDS;
	public static final Map<Integer, String[]> NAMESPACES_TAGS;
	
	static {
		NAMESPACES_IDS = new HashMap<String, Integer>();
		NAMESPACES_TAGS = new HashMap<Integer, String[]>();
		
		NAMESPACES_IDS.put("AirSync", 0x0);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Contacts", 0x1);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Email", 0x2);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("AirNotify", 0x3);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Cal", 0x4);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Move", 0x5);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("ItemEstimate", 0x6);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("FolderHierarchy", 0x7);
		NAMESPACES_TAGS.put(0x7, CP_7);
		
		NAMESPACES_IDS.put("MeetingResponse", 0x8);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Tasks", 0x9);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("ResolveRecipients", 0xA);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("ValidateCert", 0xB);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Contacts2", 0xC);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Ping", 0xD);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Provision", 0xE);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Search", 0xF);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Gal", 0x10);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("AirSyncBase", 0x11);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("Settings", 0x12);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("DocumentLibrary", 0x13);
		NAMESPACES_TAGS.put(0x0, CP_0);
		
		NAMESPACES_IDS.put("ItemOperations", 0x14);
		NAMESPACES_TAGS.put(0x0, CP_0);		
	}
	
	public static String[] getTagsTableForNamespace(String nsName) {
		Integer codePage = NAMESPACES_IDS.get(nsName);
		String[] ret = NAMESPACES_TAGS.get(codePage);
		return ret;
	}
	
}
