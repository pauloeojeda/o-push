package org.obm.push.data;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.MSContact;
import org.obm.push.backend.SyncCollection;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class ContactEncoder implements IDataEncoder {

	@Override
	public void encode(BackendSession bs, Element parent,
			IApplicationData data, SyncCollection collectio, boolean isResponse) {
		// TODO Auto-generated method stub
		MSContact c = (MSContact) data;

		// DOMUtils.createElement(parent, "Contacts:CompressedRTF");

		if (bs.getProtocolVersion() > 12) {
			Element body = DOMUtils.createElement(parent, "AirSyncBase:Body");
			e(body, "AirSyncBase:Type", "3");
			e(body, "AirSyncBase:EstimatedDataSize", "5500"); // FIXME random
																// value....
			e(body, "AirSyncBase:Truncated", "1");
		}

		DOMUtils.createElementAndText(parent, "Contacts:FileAs", getFileAs(c));

		e(parent, "Contacts:FirstName", c.getFirstName());
		e(parent, "Contacts:LastName", c.getLastName());
		e(parent, "Contacts:MiddleName", c.getMiddleName());

		e(parent, "Contacts:JobTitle", c.getJobTitle());
		e(parent, "Contacts:Department", c.getDepartment());

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

		e(parent, "Contacts:Email1Address", c.getEmail1Address());
		e(parent, "Contacts:Email2Address", c.getEmail2Address());
		e(parent, "Contacts:Email3Address", c.getEmail3Address());

		if (bs.getProtocolVersion() > 12) {
			e(parent, "AirSyncBase:NativeBodyType", "3");
		}

		// DOMUtils.createElement(parent, "Contacts:Picture");
		// DOMUtils.createElement(parent, "Contacts:Body");
	}

	private String getFileAs(MSContact c) {
		if (c.getFirstName() != null && c.getLastName() != null
				&& c.getFirstName().length() > 0) {
			return c.getLastName() + ", " + c.getFirstName();
		} else if (c.getFirstName() != null && c.getFirstName().length() > 0) {
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
