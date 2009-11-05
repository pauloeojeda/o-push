package org.obm.push.backend.obm22.mail;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.minig.imap.IMAPException;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.store.ISyncStorage;

public class MailBackend extends ObmSyncBackend {

	public MailBackend(ISyncStorage storage) {
		super(storage);
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
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
		ic.setItemType(FolderType.DEFAULT_SENT_EMAIL_FOLDER);
		ret.add(ic);

		ic = new ItemChange();
		ic.setServerId(genServerId(bs, "Trash"));
		ic.setParentId("0");
		ic.setDisplayName("Trash");
		ic.setItemType(FolderType.DEFAULT_DELETED_ITEMS_FOLDERS);
		ret.add(ic);
		return ret;
	}

	public DataDelta getContentChanges(BackendSession bs, String collection) {
		logger.info("Collection: " + collection);
		List<ItemChange> changes = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		int collectionId = getCollectionIdFor(bs.getDevId(), collection);
		try {
			int devId = getDevId(bs.getDevId());
			MailChanges mc = EmailManager.getInstance().getSync(bs, devId,
					collectionId, collection);
			changes = getChanges(bs, collection, mc.getUpdated());
			deletions.addAll(getDeletions(bs, collection, mc.getRemoved()));
			bs.setUpdatedSyncDate(mc.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return new DataDelta(changes, deletions);
	}

	private String genServerId(BackendSession bs, String imapFolder) {
		StringBuilder sb = new StringBuilder();
		sb.append("obm:\\\\");
		sb.append(bs.getLoginAtDomain());
		sb.append("\\email\\");
		sb.append(imapFolder);
		String s = sb.toString();
		return getServerIdFor(bs.getDevId(), s, null);
	}

	private List<ItemChange> getChanges(BackendSession bs, String collection,
			Set<Long> uids) {

		List<ItemChange> itch = new LinkedList<ItemChange>();
		try {
			List<MSEmail> msMails = EmailManager.getInstance().fetchMails(bs,
					collection, uids);
			for (MSEmail mail : msMails) {
				ItemChange ic = new ItemChange();
				ic.setServerId(getServerIdFor(bs.getDevId(), collection, ""
						+ mail.getUid()));
				ic.setData(mail);
				itch.add(ic);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return itch;
	}

	protected List<ItemChange> getDeletions(BackendSession bs,
			String collection, Set<Long> uids) {
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		for (Long uid : uids) {
			deletions.add(getDeletion(bs, collection, uid.toString()));
		}
		return deletions;
	}

	// public List<ItemChange> fetchItems(List<String> fetchIds) {
	// // TODO Fake data
	// LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
	//
	// ItemChange ic = new ItemChange();
	// ic.setServerId("358");
	// ic.setData(new MSMail());
	// ret.add(ic);
	//
	// return ret;
	// }

	public void delete(BackendSession bs, String serverId) {
		logger.info("delete serverId " + serverId);
		if (serverId != null) {
			Long uid = getEmailUidFor(serverId);
			Integer collectionId = getCollectionIdFor(serverId);
			String collectionName = getCollectionNameFor(collectionId);
			try {
				EmailManager.getInstance().delete(bs, collectionName, uid);
			} catch (IMAPException e) {
				logger.error(e.getMessage(),e);
			}
		}
	}

	public String createOrUpdate(BackendSession bs, String collection,
			String serverId, String clientId, MSEmail data) {
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", "
				+ collection + ", " + serverId + ", " + clientId + ")");
		if (serverId != null) {
			Long mailUid = getEmailUidFor(serverId);
			try {
				EmailManager.getInstance().updateReadFlag(bs, collection,
						mailUid, data.isRead());
			} catch (IMAPException e) {
				logger.error(e.getMessage(),e);
			}
		}

		return null;
	}

	public Long getEmailUidFor(String serverId) {
		int idx = serverId.lastIndexOf(":");
		return Long.parseLong(serverId.substring(idx + 1));
	}

	public Integer getCollectionIdFor(String serverId) {
		int idx = serverId.lastIndexOf(":");
		return Integer.parseInt(serverId.substring(0, idx));
	}

}
