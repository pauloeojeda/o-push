package org.obm.sync.push.client;

import java.io.InputStream;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TestSearch extends AbstractPushTest {

	public void testSearchIphone() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("SearchRequestIphone.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml25("Search", doc, "Search");
		assertNotNull(ret);
	}
	
	public void testSearchWM65() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("SearchRequestWM65.xml");
		Document doc = DOMUtils.parse(in);
		Element query = DOMUtils.getUniqueElement(doc.getDocumentElement(), "Query");
		query.setTextContent("Me");
		Document ret = postXml("Search", doc, "Search");
		assertNotNull(ret);
	}


}
