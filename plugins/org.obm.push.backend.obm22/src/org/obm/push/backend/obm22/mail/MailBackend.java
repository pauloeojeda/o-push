package org.obm.push.backend.obm22.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.james.mime4j.parser.MimeStreamParser;
import org.minig.imap.IMAPException;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSAttachementData;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.NotAllowedException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.calendar.CalendarClient;

import fr.aliasource.utils.FileUtils;

public class MailBackend extends ObmSyncBackend {

	private EmailManager emailManager;

	public MailBackend(ISyncStorage storage) {
		super(storage);
		emailManager = EmailManager.getInstance();
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();

		ret.add(genItemChange(bs, "INBOX", FolderType.DEFAULT_INBOX_FOLDER));
		ret.add(genItemChange(bs, "Drafts", FolderType.DEFAULT_DRAFTS_FOLDERS));
		ret
				.add(genItemChange(bs, "Sent",
						FolderType.DEFAULT_SENT_EMAIL_FOLDER));
		ret.add(genItemChange(bs, "Trash",
				FolderType.DEFAULT_DELETED_ITEMS_FOLDERS));

		return ret;
	}

	public DataDelta getContentChanges(BackendSession bs, SyncState state, String collectionPath) {
		logger.info("Collection: " + collectionPath);
		List<ItemChange> changes = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		try {
			int collectionId = getCollectionIdFor(bs.getDevId(), collectionPath);

			int devId = getDevId(bs.getDevId());
			MailChanges mc = emailManager.getSync(bs,state, devId, collectionId,
					collectionPath);
			changes = getChanges(bs, collectionId, collectionPath, mc.getUpdated());
			deletions.addAll(getDeletions(bs, collectionPath, mc.getRemoved()));
			bs.addUpdatedSyncDate(
					getCollectionIdFor(bs.getDevId(), collectionPath), mc
							.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new DataDelta(changes, deletions);
	}

	private ItemChange genItemChange(BackendSession bs, String imapFolder,
			FolderType type) {
		ItemChange ic = new ItemChange();
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " " + imapFolder);
		ic.setItemType(type);

		StringBuilder sb = new StringBuilder();
		sb.append("obm:\\\\");
		sb.append(bs.getLoginAtDomain());
		sb.append("\\email\\");
		sb.append(imapFolder);
		String s = buildPath(bs, imapFolder);
		String serverId;
		try {
			serverId = getServerIdFor(bs.getDevId(), s, null);
		} catch (ActiveSyncException e) {
			serverId = createCollectionMapping(bs.getDevId(), sb.toString());
			ic.setIsNew(true);
		}

		ic.setServerId(serverId);
		return ic;
	}
	
	private String buildPath(BackendSession bs, String imapFolder){
		StringBuilder sb = new StringBuilder();
		sb.append("obm:\\\\");
		sb.append(bs.getLoginAtDomain());
		sb.append("\\email\\");
		sb.append(imapFolder);
		return sb.toString();
	}

	public String getWasteBasketPath(BackendSession bs) {
		return buildPath(bs, "Trash");
	}
	
	private List<ItemChange> getChanges(BackendSession bs,
			Integer collectionId, String collection, Set<Long> uids) {

		List<ItemChange> itch = new LinkedList<ItemChange>();
		try {

			List<MSEmail> msMails = emailManager.fetchMails(bs,
					getCalendarClient(bs), collectionId, collection, uids);
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
			String collection, Set<Long> uids) throws ActiveSyncException {
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		for (Long uid : uids) {
			deletions.add(getDeletion(bs, collection, uid.toString()));
		}
		return deletions;
	}

	public List<ItemChange> fetchItems(BackendSession bs, List<String> fetchIds)
			throws ActiveSyncException {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		for (String serverId : fetchIds) {
			Integer collectionId = getCollectionIdFor(serverId);
			String collectionName = getCollectionNameFor(collectionId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(getItemIdFor(serverId).longValue());
			List<MSEmail> emails;
			try {
				emails = emailManager.fetchMails(bs, getCalendarClient(bs),
						collectionId, collectionName, uids);
				if (emails.size() > 0) {
					MSEmail email = emails.get(0);
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

	public void delete(BackendSession bs, String serverId, Boolean moveToTrash) {
		if(moveToTrash){
			logger.info("move to trash serverId " + serverId);
		} else {
			logger.info("delete serverId " + serverId);
		}
		if (serverId != null) {
			try {
				Long uid = getItemIdFor(serverId).longValue();
				Integer collectionId = getCollectionIdFor(serverId);
				String collectionName = getCollectionNameFor(collectionId);
				Integer devId = getDevId(bs.getDevId());
				
				if(moveToTrash){
					String wasteBasketPath = getWasteBasketPath(bs);
					Integer wasteBasketId = getCollectionIdFor(bs.getDevId(), wasteBasketPath);
					emailManager.moveItem(bs, devId, collectionName, collectionId, wasteBasketPath, wasteBasketId, uid);
				} else {
					emailManager.delete(bs, devId, collectionName, collectionId,
						uid);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public String createOrUpdate(BackendSession bs, String collection,
			String serverId, String clientId, MSEmail data) {
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", "
				+ collection + ", " + serverId + ", " + clientId + ")");
		if (serverId != null) {
			Long mailUid = getItemIdFor(serverId).longValue();
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
		Integer srcFolderId = null;
		Integer dstFolderId = null;
		Long newUidMail = null;
		try {
			srcFolderId = getCollectionIdFor(bs.getDevId(), srcFolder);

			dstFolderId = getCollectionIdFor(bs.getDevId(), dstFolder);
			Integer devId = getDevId(bs.getDevId());

			newUidMail = emailManager.moveItem(bs, devId, srcFolder,
					srcFolderId, dstFolder, dstFolderId,
					getItemIdFor(messageId).longValue());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (newUidMail == null) {
			return null;
		}
		return dstFolderId + ":" + newUidMail;
	}

	private Integer getCollectionIdFor(String serverId) {
		int idx = serverId.lastIndexOf(":");
		Integer collectionId = 0;
		if (idx > 0) {
			collectionId = Integer.parseInt(serverId.substring(0, idx));
		}
		return collectionId;
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
			Long uid = getItemIdFor(serverId).longValue();
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<MSEmail> mail = emailManager.fetchMails(bs,
					getCalendarClient(bs), Integer.parseInt(collectionId),
					collectionName, uids);

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
			Long uid = getItemIdFor(serverId).longValue();
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<InputStream> mail = emailManager.fetchMIMEMails(bs,
					getCalendarClient(bs), collectionName, uids);

			if (mail.size() > 0) {
				ForwardEmailHandler reh = new ForwardEmailHandler(
						getUserEmail(bs), mail.get(0));
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
			String serverId) throws ActiveSyncException {
		String collectionName = getCollectionNameFor(collectionId);
		Long uid = getItemIdFor(serverId).longValue();
		Set<Long> uids = new HashSet<Long>();
		uids.add(uid);
		List<MSEmail> emails;
		try {
			emails = emailManager.fetchMails(bs, getCalendarClient(bs),
					collectionId, collectionName, uids);
			if (emails.size() > 0) {
				return emails.get(0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public MSAttachementData getAttachment(BackendSession bs,
			String attachmentId) throws ObjectNotFoundException {
		if (attachmentId != null && !attachmentId.isEmpty()) {
			Map<String, String> parsedAttId = AttachmentHelper
					.parseAttachmentId(attachmentId);
			try {
				String collectionId = parsedAttId
						.get(AttachmentHelper.COLLECTION_ID);
				String messageId = parsedAttId.get(AttachmentHelper.MESSAGE_ID);
				String mimePartAddress = parsedAttId
						.get(AttachmentHelper.MIME_PART_ADDRESS);
				String contentType = parsedAttId
						.get(AttachmentHelper.CONTENT_TYPE);
				String contentTransferEncoding = parsedAttId
						.get(AttachmentHelper.CONTENT_TRANSFERE_ENCODING);
				logger.info("attachmentId= [collectionId:" + collectionId
						+ "] [emailUid" + messageId + "] [mimePartAddress:"
						+ mimePartAddress + "] [contentType" + contentType
						+ "] [contentTransferEncoding"
						+ contentTransferEncoding + "]");

				String collectionName = getCollectionNameFor(Integer
						.parseInt(collectionId));
				InputStream is = emailManager.findAttachment(bs,
						collectionName, Long.parseLong(messageId),
						mimePartAddress);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				FileUtils.transfer(is, out, true);
				byte[] rawData = out.toByteArray();

				if ("QUOTED-PRINTABLE".equals(contentTransferEncoding)) {
					out = new ByteArrayOutputStream();
					InputStream in = new QuotedPrintableDecoderInputStream(
							new ByteArrayInputStream(rawData));
					FileUtils.transfer(in, out, true);
					rawData = out.toByteArray();
				} else if ("BASE64".equals(contentTransferEncoding)) {
					rawData = new Base64().decode(rawData);
				}

				return new MSAttachementData(contentType,
						new ByteArrayInputStream(rawData));
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
		throw new ObjectNotFoundException();
	}

	public void purgeFolder(BackendSession bs, String collectionPath, boolean deleteSubFolder) throws CollectionNotFoundException, NotAllowedException {
		String wasteBasketPath = getWasteBasketPath(bs);
		if(!wasteBasketPath.equals(collectionPath)){
			throw new NotAllowedException("Only the Trash folder can be purged.");
		}
		try {
			
			int devId = getDevId(bs.getDevId());
			int collectionId = getCollectionIdFor(bs.getDevId(), collectionPath);
			emailManager.purgeFolder(bs, devId, collectionPath, collectionId);
			if(deleteSubFolder){
				logger.warn("deleteSubFolder isn't implemented because opush doesn't yet manage folders");
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new NotAllowedException(e);
		}
	}
}
