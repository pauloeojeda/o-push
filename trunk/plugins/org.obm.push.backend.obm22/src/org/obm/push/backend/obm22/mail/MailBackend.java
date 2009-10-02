package org.obm.push.backend.obm22.mail;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSMail;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.store.ISyncStorage;

public class MailBackend extends ObmSyncBackend {

	public static final String FOLDER_PREFIX = "obm:\\\\mail\\";

	public MailBackend(ISyncStorage storage) {
		super(storage);
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		// TODO Auto-generated method stub
		ItemChange ic = new ItemChange();
		ic.setServerId(genServerId(bs, "INBOX"));
		ic.setParentId("0");
		ic.setDisplayName("Inbox");
		ic.setItemType(FolderType.DEFAULT_INBOX_FOLDER);
		ret.add(ic);

		ic = new ItemChange();
		ic.setServerId(genServerId(bs, "Sent"));
		ic.setParentId("0");
		ic.setDisplayName("Sent");
		ic.setItemType(FolderType.DEFAULT_SENT_MAIL_FOLDER);
		ret.add(ic);

		ic = new ItemChange();
		ic.setServerId(genServerId(bs, "Trash"));
		ic.setParentId("0");
		ic.setDisplayName("Trash");
		ic.setItemType(FolderType.DEFAULT_DELETED_ITEMS_FOLDERS);
		ret.add(ic);

		return ret;

	}

	private String genServerId(BackendSession bs, String imapFolder) {
		StringBuilder sb = new StringBuilder(FOLDER_PREFIX);
		sb.append(bs.getLoginAtDomain());
		sb.append('\\');
		sb.append(imapFolder);
		String s = sb.toString();
		return getServerIdFor(bs.getDevId(), s, null);
	}

	public DataDelta getContentChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		LinkedList<ItemChange> deletions = new LinkedList<ItemChange>();

		// FIXME fake data
		ItemChange ic = new ItemChange();
		ic.setServerId("358");
		ic.setData(new MSMail());
		ret.add(ic);

		return new DataDelta(ret, deletions);
	}

	public List<ItemChange> fetchItems(List<String> fetchIds) {
		// TODO Fake data
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		ic.setServerId("358");
		ic.setData(new MSMail());
		ret.add(ic);

		return ret;
	}

	public void delete(BackendSession bs, String serverId) {
		// TODO Auto-generated method stub

	}

}
