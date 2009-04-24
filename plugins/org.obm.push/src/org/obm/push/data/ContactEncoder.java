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

		e(parent, "Contacts:BusinessAddressStreet", c.getBusinessStreet());
		e(parent, "Contacts:BusinessAddressPostalCode", c
				.getBusinessPostalCode());
		e(parent, "Contacts:BusinessAddressCity", c.getBusinessAddressCity());
		e(parent, "Contacts:BusinessAddressCountry", c
				.getBusinessAddressCountry());
		e(parent, "Contacts:BusinessAddressState", c.getBusinessState());

		e(parent, "Contacts:HomeAddressStreet", c.getHomeAddressStreet());
		e(parent, "Contacts:HomeAddressPostalCode", c
				.getHomeAddressPostalCode());
		e(parent, "Contacts:HomeAddressCity", c.getHomeAddressCity());
		e(parent, "Contacts:HomeAddressCountry", c.getHomeAddressCountry());
		e(parent, "Contacts:HomeAddressState", c.getHomeAddressState());

		e(parent, "Contacts:OtherAddressStreet", c.getOtherAddressStreet());
		e(parent, "Contacts:OtherAddressPostalCode", c
				.getOtherAddressPostalCode());
		e(parent, "Contacts:OtherAddressCity", c.getOtherAddressCity());
		e(parent, "Contacts:OtherAddressCountry", c.getOtherAddressCountry());
		e(parent, "Contacts:OtherAddressState", c.getOtherAddressState());

		e(parent, "Contacts:HomeTelephoneNumber", c.getHomePhoneNumber());
		e(parent, "Contacts:Home2TelephoneNumber", c.getHome2PhoneNumber());
		e(parent, "Contacts:MobileTelephoneNumber", c.getMobilePhoneNumber());
		e(parent, "Contacts:BusinessTelephoneNumber", c
				.getBusinessPhoneNumber());
		e(parent, "Contacts:Business2TelephoneNumber", c
				.getBusiness2PhoneNumber());
		e(parent, "Contacts:HomeFaxNumber", c.getHomeFaxNumber());
		e(parent, "Contacts:BusinessFaxNumber", c.getBusinessFaxNumber());

		// e(parent, "Contacts2:IMAddress", c.getIMAddress());
		// e(parent, "Contacts2:IMAddress2", c.getIMAddress2());
		// e(parent, "Contacts2:IMAddress3", c.getIMAddress3());

		DOMUtils.createElement(parent, "Contacts:CompressedRTF");
		DOMUtils.createElement(parent, "Contacts:Picture");
	}

	private String getFileAs(MSContact c) {
		if (c.getFirstName() != null && c.getLastName() != null) {
			return c.getLastName() + ", " + c.getFirstName();
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
