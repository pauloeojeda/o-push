package org.obm.push.backend.obm22.mail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.james.mime4j.parser.MimeStreamParser;
import org.minig.imap.IMAPException;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.store.ISyncStorage;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.calendar.CalendarClient;

public class MailBackend extends ObmSyncBackend {

	private EmailManager emailManager;

	public MailBackend(ISyncStorage storage) {
		super(storage);
		emailManager = EmailManager.getInstance();
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
			MailChanges mc = emailManager.getSync(bs, devId, collectionId,
					collection);
			changes = getChanges(bs, collection, mc.getUpdated());
			deletions.addAll(getDeletions(bs, collection, mc.getRemoved()));
			bs.setUpdatedSyncDate(mc.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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

			List<MSEmail> msMails = emailManager.fetchMails(bs,
					getCalendarClient(bs), collection, uids);
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

	public List<ItemChange> fetchItems(BackendSession bs, List<String> fetchIds) {
		// TODO Fake data
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();

		Map<String, Set<Long>> uidByCollecionName = new HashMap<String, Set<Long>>();
		for (String serverId : fetchIds) {
			Integer collectionId = getCollectionIdFor(serverId);
			String collectionName = getCollectionNameFor(collectionId);
			Set<Long> uids = uidByCollecionName.get(collectionName);
			if (uids == null) {
				uids = new HashSet<Long>();
				uidByCollecionName.put(collectionName, uids);
			}
			uids.add(getEmailUidFor(serverId));
		}

		for (Entry<String, Set<Long>> entry : uidByCollecionName.entrySet()) {
			String collectionName = entry.getKey();

			try {
				List<MSEmail> emails = emailManager
						.fetchMails(bs, getCalendarClient(bs), collectionName,
								entry.getValue());
				
				for (MSEmail email : emails) {
					ItemChange ic = new ItemChange();
					ic.setServerId(getServerIdFor(bs.getDevId(),
							collectionName, "" + email.getUid()));
					ic.setData(email);
					ret.add(ic);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return ret;
	}

	public void delete(BackendSession bs, String serverId) {
		logger.info("delete serverId " + serverId);
		if (serverId != null) {
			Long uid = getEmailUidFor(serverId);
			Integer collectionId = getCollectionIdFor(serverId);
			String collectionName = getCollectionNameFor(collectionId);
			Integer devId = getDevId(bs.getDevId());
			try {
				emailManager.delete(bs, devId, collectionName, collectionId,
						uid);
			} catch (IMAPException e) {
				logger.error(e.getMessage(), e);
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
				emailManager.updateReadFlag(bs, collection, mailUid, data
						.isRead());
			} catch (IMAPException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return null;
	}

	public String move(BackendSession bs, String srcFolder, String dstFolder,
			String messageId) {
		logger.info("move(" + bs.getLoginAtDomain() + ", messageId "
				+ messageId + " from " + srcFolder + " to " + dstFolder + ")");
		Integer srcFolderId = getCollectionIdFor(bs.getDevId(), srcFolder);
		Integer dstFolderId = getCollectionIdFor(bs.getDevId(), dstFolder);
		Integer devId = getDevId(bs.getDevId());
		Long newUidMail = null;
		try {
			newUidMail = emailManager.moveItem(bs, devId, srcFolder,
					srcFolderId, dstFolder, dstFolderId,
					getEmailUidFor(messageId));
		} catch (IMAPException e) {
			logger.error(e.getMessage(), e);
		}
		if (newUidMail == null) {
			return null;
		}
		return dstFolderId + ":" + newUidMail;
	}

	private Long getEmailUidFor(String serverId) {
		int idx = serverId.lastIndexOf(":");
		return Long.parseLong(serverId.substring(idx + 1));
	}

	private Integer getCollectionIdFor(String serverId) {
		int idx = serverId.lastIndexOf(":");
		return Integer.parseInt(serverId.substring(0, idx));
	}

	public void sendEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent) {
		try {
			SendEmailHandler handler = new SendEmailHandler(getUserEmail(bs));
			send(bs, mailContent, handler, saveInSent);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void replyEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent, String collectionId, String serverId) {
		try {
			String collectionName = getCollectionNameFor(Integer
					.parseInt(collectionId));
			Long uid = getEmailUidFor(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<MSEmail> mail = emailManager.fetchMails(bs,
					getCalendarClient(bs), collectionName, uids);

			if (mail.size() > 0) {
				ReplyEmailHandler reh = new ReplyEmailHandler(getUserEmail(bs),
						mail.get(0));
				send(bs, mailContent, reh, saveInSent);
				emailManager.setAnsweredFlag(bs, collectionName, uid);
			} else {
				SendEmailHandler handler = new SendEmailHandler(
						getUserEmail(bs));
				send(bs, mailContent, handler, saveInSent);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void forwardEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent, String collectionId, String serverId) {
		try {
			String collectionName = getCollectionNameFor(Integer
					.parseInt(collectionId));
			Long uid = getEmailUidFor(serverId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<InputStream> mail = emailManager.fetchMIMEMails(bs,
					getCalendarClient(bs), collectionName, uids);

			if (mail.size() > 0) {
				ForwardEmailHandler reh = new ForwardEmailHandler(getUserEmail(bs),
						mail.get(0));
				send(bs, mailContent, reh, saveInSent);
				emailManager.setAnsweredFlag(bs, collectionName, uid);
			} else {
				SendEmailHandler handler = new SendEmailHandler(
						getUserEmail(bs));
				send(bs, mailContent, handler, saveInSent);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private String getUserEmail(BackendSession bs) throws Exception {
		CalendarClient cal = getCalendarClient(bs);
		AccessToken at = cal.login(bs.getLoginAtDomain(), bs.getPassword(),
				"opush");
		String from = "";
		try {
			from = cal.getUserEmail(at);
		} finally {
			cal.logout(at);
		}
		return from;
	}

	private void send(BackendSession bs, byte[] mailContent,
			SendEmailHandler handler, Boolean saveInSent) throws Exception {
		MimeStreamParser parser = new MimeStreamParser();
		parser.setContentHandler(handler);

		parser.parse(new ByteArrayInputStream(mailContent));
		emailManager.sendEmail(bs, handler.getFrom(), handler.getTo(), handler
				.getMessage(), saveInSent);
	}

	public MSEmail getEmail(BackendSession bs, Integer collectionId,
			String serverId) {
		String collectionName = getCollectionNameFor(collectionId);
		Long uid = getEmailUidFor(serverId);
		Set<Long> uids = new HashSet<Long>();
		uids.add(uid);
		List<MSEmail> emails;
		try {
			emails = emailManager.fetchMails(bs, getCalendarClient(bs),
					collectionName, uids);
			if (emails.size() > 0) {
				return emails.get(0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
