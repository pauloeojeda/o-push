package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class TestProvision extends AbstractPushTest {

	public void testSettingsProvisionProtocol121() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Provision", doc, "Provision");
		assertNotNull(ret);
	}

	public void testSettingsProvisionProtocol25() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequestProtocol2.5.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Provision", doc, "Provision", "0", "2.5");
		assertNotNull(ret);
	}
}
