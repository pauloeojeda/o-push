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
