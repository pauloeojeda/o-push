package org.obm.push.backend.obm22.mail;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.Mail;

public class MailExporter {

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		ItemChange ic = new ItemChange();
		ic.setServerId("INBOX");
		ic.setParentId("0");
		ic.setDisplayName("Inbox");
		ic.setItemType(FolderType.DEFAULT_INBOX_FOLDER);
		ret.add(ic);

		ic = new ItemChange();
		ic.setServerId("Sent");
		ic.setParentId("0");
		ic.setDisplayName("Sent");
		ic.setItemType(FolderType.DEFAULT_SENT_MAIL_FOLDER);
		ret.add(ic);

		 ic = new ItemChange();
		 ic.setServerId("obm://mail/user@domain/Trash");
		 ic.setParentId("0");
		 ic.setDisplayName("Inbox");
		 ic.setItemType(FolderType.DEFAULT_DELETED_ITEMS_FOLDERS);
		 ret.add(ic);

		return ret;

	}

	public List<ItemChange> getContentChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		
		// FIXME fake data
		ItemChange ic = new ItemChange();
		ic.setDisplayName("Mail subject");
		ic.setServerId("358");
		ic.setParentId("0");
		ic.setData(new Mail());
		ret.add(ic);

		return ret;
	}

	public List<ItemChange> fetchItems(List<String> fetchIds) {
		// TODO Fake data
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		
		ItemChange ic = new ItemChange();
		ic.setServerId("358");
		ic.setData(new Mail());
		ret.add(ic);

		return ret;
	}
	
}
