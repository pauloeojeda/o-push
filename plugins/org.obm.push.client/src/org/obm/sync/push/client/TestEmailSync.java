package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class TestEmailSync extends AbstractPushTest {

	public void testMailSync() throws Exception {
		InputStream in = loadDataFile("EmailSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);
	}

}
