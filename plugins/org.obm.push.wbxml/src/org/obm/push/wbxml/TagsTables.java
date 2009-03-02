package org.obm.push.wbxml;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TagsTables {

	private static final Log logger = LogFactory.getLog(TagsTables.class);

	/**
	 * AirSync
	 */
	public static final String[] CP_0 = {
	// AirSync
			"Sync", // 0x05
			"Responses", // 0x06
			"Add", // 0x07
			"Change", // 0x08
			"Delete", // 0x09
			"Fetch", // 0x0A
			"SyncKey", // 0x0B
			"ClientId", // 0x0C
			"ServerId", // 0x0D
			"Status", // 0x0E
			"Collection", // 0x0F
			"Class", // 0x10
			"Version", // 0x11
			"CollectionId", // 0x12
			"GetChanges", // 0x13
			"MoreAvailable", // 0x14
			"WindowSize", // 0x15
			"Commands", // 0x16
			"Options", // 0x17
			"FilterType", // 0x18
			"Truncation", // 0x19
			"RTFTruncation", // 0x1A
			"Conflict", // 0x1B
			"Collections", // 0x1C
			"ApplicationData", // 0x1D
			"DeletesAsMoves", // 0x1E
			"NotifyGUID", // 0x1F
			"Supported", // 0x20
			"SoftDelete", // 0x21
			"MIMESupport", // 0x22
			"MIMETruncation", // 0x23
			"Wait", // 0x24
			"Limit", // 0x25
			"Partial", // 0x26
	};

	public static final String[] CP_1 = {
	// Contacts
			"Anniversary", // 0x05
			"AssistantName", // 0x06
			"AssistantTelephoneNumber", // 0x07
			"Birthday", // 0x08
			"Body", // 0x09
			"BodySize", // 0x0A
			"BodyTruncated", // 0x0B
			"Business2TelephoneNumber", // 0x0C
			"BusinessAddressCity", // 0x0D
			"BusinessAddressCountry", // 0x0E
			"BusinessAddressPostalCode", // 0x0F
			"BusinessAddressState", // 0x10
			"BusinessAddressStreet", // 0x11
			"BusinessFaxNumber", // 0x12
			"BusinessTelephoneNumber", // 0x13
			"CarTelephoneNumber", // 0x14
			"Categories", // 0x15
			"Category", // 0x16
			"Children", // 0x17
			"Child", // 0x18
			"CompanyName", // 0x19
			"Department", // 0x1A
			"Email1Address", // 0x1B
			"Email2Address", // 0x1C
			"Email3Address", // 0x1D
			"FileAs", // 0x1E
			"FirstName", // 0x1F
			"Home2TelephoneNumber", // 0x20
			"HomeAddressCity", // 0x21
			"HomeAddressCountry", // 0x22
			"HomeAddressPostalCode", // 0x23
			"HomeAddressState", // 0x24
			"HomeAddressStreet", // 0x25
			"HomeFaxNumber", // 0x26
			"HomeTelephoneNumber", // 0x27
			"JobTitle", // 0x28
			"LastName", // 0x29
			"MiddleName", // 0x2A
			"MobileTelephoneNumber", // 0x2B
			"OfficeLocation", // 0x2C
			"OtherAddressCity", // 0x2D
			"OtherAddressCountry", // 0x2E
			"OtherAddressPostalCode", // 0x2F
			"OtherAddressState", // 0x30
			"OtherAddressStreet", // 0x31
			"PagerNumber", // 0x32
			"RadioTelephoneNumber", // 0x33
			"Spouse", // 0x34
			"Suffix", // 0x35
			"Title", // 0x36
			"Webpage", // 0x37
			"YomiCompanyName", // 0x38
			"YomiFirstName", // 0x39
			"YomiLastName", // 0x3A
			"CompressedRTF", // 0x3B
			"Picture", // 0x3C
	};

	public static final String[] CP_6 = {
	// ItemEstimate
			"GetItemEstimate", // 0x05
			"Version", // 0x06
			"Collections", // 0x07
			"Collection", // 0x08
			"Class", // 0x09
			"CollectionId", // 0x0A
			"DateTime", // 0x0B
			"Estimate", // 0x0C
			"Response", // 0x0D
			"Status", // 0x0E
	};

	/**
	 * FolderHierarchy
	 */
	public static final String[] CP_7 = {
	// FolderHierarchy
			"Folders", // 0x05
			"Folder", // 0x06
			"DisplayName", // 0x07
			"ServerId", // 0x08
			"ParentId", // 0x09
			"Type", // 0x0A
			"Response", // Ox0B
			"Status", // 0x0C
			"ContentClass", // 0x0D
			"Changes", // 0x0E
			"Add", // 0x0F
			"Delete", // 0x10
			"Update", // 0x11
			"SyncKey", // 0x12
			"FolderCreate", // 0x13
			"FolderDelete", // 0x14
			"FolderUpdate", // 0x15
			"FolderSync", // 0x16
			"Count", // 0x17
			"Version", // 0x18
	};

	public static final Map<String, Integer> NAMESPACES_IDS;
	public static final Map<Integer, String[]> NAMESPACES_TAGS;
	public static final Map<String, Map<String, Integer>> NAMESPACES_MAPPINGS;

	static {
		NAMESPACES_IDS = new HashMap<String, Integer>();
		NAMESPACES_TAGS = new HashMap<Integer, String[]>();
		NAMESPACES_MAPPINGS = new HashMap<String, Map<String, Integer>>();

		NAMESPACES_IDS.put("AirSync", 0x0);
		NAMESPACES_TAGS.put(0x0, CP_0);
		createMappings("AirSync");

		NAMESPACES_IDS.put("Contacts", 0x1);
		NAMESPACES_TAGS.put(0x1, CP_1);
		createMappings("Contacts");

		NAMESPACES_IDS.put("ItemEstimate", 0x6);
		NAMESPACES_TAGS.put(0x6, CP_6);
		createMappings("ItemEstimate");

		NAMESPACES_IDS.put("FolderHierarchy", 0x7);
		NAMESPACES_TAGS.put(0x7, CP_7);
		createMappings("FolderHierarchy");

	}

	public static String[] getTagsTableForNamespace(String nsName) {
		Integer codePage = NAMESPACES_IDS.get(nsName);
		String[] ret = NAMESPACES_TAGS.get(codePage);
		return ret;
	}

	private static void createMappings(String namespace) {
		Integer tableId = NAMESPACES_IDS.get(namespace);
		logger.info("id for namespace '" + namespace + "' is "
				+ Integer.toHexString(tableId));
		String[] stab = NAMESPACES_TAGS.get(tableId);
		int start = 0x05;
		Map<String, Integer> mapping = new HashMap<String, Integer>();
		for (String tag : stab) {
			mapping.put(tag, start++);
		}
		NAMESPACES_MAPPINGS.put(namespace, mapping);
	}

	public static Map<String, Integer> getElementMappings(String newNs) {
		return NAMESPACES_MAPPINGS.get(newNs);
	}

}
