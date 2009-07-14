package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

public class TestSearch extends AbstractPushTest {

	public void testSearch() throws Exception {
//		optionsQuery();

		InputStream in = loadDataFile("SearchRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Search", doc, "Search");
		assertNotNull(ret);
	}

}
