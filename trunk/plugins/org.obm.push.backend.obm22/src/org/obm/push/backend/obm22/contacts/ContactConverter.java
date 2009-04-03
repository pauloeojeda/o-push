package org.obm.push.backend.obm22.contacts;

import org.obm.push.backend.MSContact;
import org.obm.sync.book.Contact;

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
		
		return oc;
	}

}
