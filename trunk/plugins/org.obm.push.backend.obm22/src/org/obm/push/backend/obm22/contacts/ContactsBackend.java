package org.obm.push.backend.obm22.contacts;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.locators.AddressBookLocator;

public class ContactsBackend extends ObmSyncBackend {

	public ContactsBackend() {
	}

	private BookClient getClient(BackendSession bs) {
		AddressBookLocator abl = new AddressBookLocator();
		if (obmSyncHost == null) {
			locateObmSync(bs);
		}
		BookClient bookCli = abl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		return bookCli;
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		ic.setServerId("obm:\\\\" + bs.getLoginAtDomain() + "\\contacts");
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " contacts");
		ic.setItemType(FolderType.DEFAULT_CONTACTS_FOLDER);
		ret.add(ic);
		return ret;
	}

}
