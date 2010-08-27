package org.obm.push.impl;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMDumper {

	/**
	 * Seeing email/cal/contact data is a security issue for some
	 * administrators. Remove data from a copy of the DOM before printing.
	 * 
	 * @param doc
	 */
	public static void dumpXml(Log logger, String prefix, Document doc) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			logger.info("before clone");
			Document c = DOMUtils.cloneDOM(doc);
			logger.info("after clone");
			NodeList nl = c.getElementsByTagName("ApplicationData");
			for (int i = 0; i < nl.getLength(); i++) {
				Node e = nl.item(i);
				NodeList children = e.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node child = children.item(j);
					e.removeChild(child);
				}

				e.setTextContent("[trimmed_output]");
			}

			DOMUtils.serialise(c, out, true);
			logger.info(prefix + out.toString());
		} catch (TransformerException e) {
		}
	}
}
