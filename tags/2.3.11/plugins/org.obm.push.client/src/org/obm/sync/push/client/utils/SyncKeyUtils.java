package org.obm.sync.push.client.utils;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class SyncKeyUtils {
	
	private SyncKeyUtils(){
		
	}
	
	public static String getFolderSyncKey(Document doc){
		return DOMUtils.getElementText(doc.getDocumentElement(), "SyncKey");
	}
	
	public static void appendFolderSyncKey(Document doc, String syncKey){
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "SyncKey").setTextContent(syncKey);
	}
}
