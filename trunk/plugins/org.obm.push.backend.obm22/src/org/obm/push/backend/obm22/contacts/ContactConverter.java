package org.obm.push.backend.obm22.contacts;

import org.obm.push.backend.MSContact;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.book.Phone;

/**
 * Converts between OBM & MS Exchange contact models
 * 
 * @author tom
 * 
 */
public class ContactConverter {

	public MSContact convert(Contact c) {
		MSContact msc = new MSContact();

		msc.setFirstName(c.getFirstname());
		msc.setLastName(c.getLastname());

		return msc;
	}

	public Contact contact(MSContact c) {
		Contact oc = new Contact();

		oc.setFirstname(c.getFirstName());
		oc.setLastname(c.getLastName());

		addPhone(oc, "HOME;VOICE;X-OBM-Ref1", c.getHomePhoneNumber());
		addPhone(oc, "OTHER;VOICE;X-OBM-Ref1", c.getHome2PhoneNumber());
		addPhone(oc, "WORK;VOICE;X-OBM-Ref1", c.getBusinessPhoneNumber());
		addPhone(oc, "WORK;VOICE;X-OBM-Ref2", c.getBusiness2PhoneNumber());
		addPhone(oc, "CELL;VOICE;X-OBM-Ref1", c.getMobilePhoneNumber());

		addPhone(oc, "WORK;FAX;X-OBM-Ref1", c.getBusinessFaxNumber());
		addPhone(oc, "HOME;FAX;X-OBM-Ref1", c.getHomeFaxNumber());

		addEmail(oc, "INTERNET;X-OBM-Ref1", c.getEmail1Address());
		addEmail(oc, "INTERNET;X-OBM-Ref2", c.getEmail2Address());
		addEmail(oc, "INTERNET;X-OBM-Ref3", c.getEmail3Address());

		addAddress(oc, "", c.getBusinessStreet(), c.getBusinessPostalCode(), c
				.getBusinessAddressCity(), c.getBusinessAddressCountry(), c
				.getBusinessState());

		return oc;
	}

	private void addAddress(Contact oc, String lbl, String street,
			String postalCode, String city, String country, String state) {
		oc.addAddress(lbl, new Address(street, postalCode, null, city, country,
				state));
	}

	private void addEmail(Contact oc, String label, String email) {
		if (email != null) {
			oc.addEmail(label, new Email(email));
		}
	}

	private void addPhone(Contact obmContact, String label, String msPhone) {
		if (msPhone != null) {
			obmContact.addPhone(label, new Phone(msPhone));
		}
	}

}
