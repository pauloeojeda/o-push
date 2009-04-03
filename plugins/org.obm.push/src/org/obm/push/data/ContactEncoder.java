package org.obm.push.data;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.MSContact;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class ContactEncoder implements IDataEncoder {

	@Override
	public void encode(BackendSession bs, Element parent, IApplicationData data) {
		// TODO Auto-generated method stub
		MSContact c = (MSContact) data;
		
		DOMUtils.createElementAndText(parent, "Contacts:FileAs", getFileAs(c));

		e(parent, "Contacts:FirstName", c.getFirstName());
		e(parent, "Contacts:LastName", c.getLastName());
		
		DOMUtils.createElement(parent, "Contacts:CompressedRTF");
		DOMUtils.createElement(parent, "Contacts:Picture");
	}

	private String getFileAs(MSContact c) {
		if (c.getFirstName() != null && c.getLastName() != null) {
			return c.getLastName()+", "+c.getFirstName();
		} else if (c.getFirstName() != null) {
			return c.getFirstName();
		} else {
			return c.getLastName();
		}
	}

	private void e(Element p, String name, String val) {
		if (val != null && val.length() > 0) {
			DOMUtils.createElementAndText(p, name, val);
		}
	}

}
