package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class TestSettings extends AbstractPushTest {

	public void testSettings() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("SettingsRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Settings", doc, "Settings");
		assertNotNull(ret);
	}

}
