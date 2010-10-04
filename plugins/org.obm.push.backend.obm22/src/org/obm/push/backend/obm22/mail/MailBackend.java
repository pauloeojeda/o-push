package org.obm.push.backend.obm22.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import org.obm.push.exception.ServerErrorException;
import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.InvitationStatus;
import org.obm.push.tnefconverter.EmailConverter;
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
			Integer collectionId = getCollectionIdFor(bs.getDevId(), s);
			serverId = getServerIdFor(collectionId, null);
		} catch (ActiveSyncException e) {
			serverId = createCollectionMapping(bs.getDevId(), sb.toString());
			ic.setIsNew(true);
		}

		ic.setServerId(serverId);
		return ic;
	}

	private String buildPath(BackendSession bs, String imapFolder) {
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

	public DataDelta getContentChanges(BackendSession bs, SyncState state,
			Integer collectionId) throws ActiveSyncException {
		String collectionPath = getCollectionPathFor(collectionId);
		logger.info("Collection: " + collectionPath);
		List<ItemChange> changes = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		try {
			int devId = getDevId(bs.getDevId());
			MailChanges mc = emailManager.getSync(bs, state, devId,
					collectionId, collectionPath);
			changes = getChanges(bs, collectionId, collectionPath, mc
					.getUpdated());
			deletions.addAll(getDeletions(collectionId, mc.getRemoved()));
			bs.addUpdatedSyncDate(getCollectionIdFor(bs.getDevId(),
					collectionPath), mc.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		DataDelta ret = new DataDelta(changes, deletions);
		filtreInvitation(bs, state, ret, collectionId);
		return ret;
	}

	private List<ItemChange> getChanges(BackendSession bs,
			Integer collectionId, String collection, Set<Long> uids) {

		List<ItemChange> itch = new LinkedList<ItemChange>();
		try {

			List<MSEmail> msMails = emailManager.fetchMails(bs,
					getCalendarClient(bs), collectionId, collection, uids);
			for (MSEmail mail : msMails) {
				ItemChange ic = new ItemChange();
				ic
						.setServerId(getServerIdFor(collectionId, ""
								+ mail.getUid()));
				ic.setData(mail);
				itch.add(ic);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return itch;
	}

	private void filtreInvitation(BackendSession bs, SyncState state,
			DataDelta delta, Integer emailCollectionId) {
		try {

			String calPath = getDefaultCalendarName(bs);
			Integer eventCollectionId = getCollectionIdFor(bs.getDevId(),
					calPath);

			List<Long> emailToSync = storage.getEmailToSynced(
					emailCollectionId, state.getKey());
			System.err.println(emailToSync.size()+" email to sync");
			List<String> emailServerId = new ArrayList<String>(emailToSync
					.size());
			for (Long emailUid : emailToSync) {
				emailServerId.add(getServerIdFor(emailCollectionId, ""
						+ emailUid));
			}
			List<ItemChange> itemToSync = fetchItems(bs, emailServerId);
			storage.updateInvitationStatus(InvitationStatus.EMAIL_SYNCED, state
					.getKey(), eventCollectionId, emailCollectionId,
					emailToSync.toArray(new Long[0]));
			delta.getChanges().addAll(itemToSync);

			for (Iterator<ItemChange> it = delta.getChanges().iterator(); it
					.hasNext();) {
				ItemChange ic = it.next();
				MSEmail mail = (MSEmail) ic.getData();
				if (mail.getInvitation() != null) {

					if (!storage.isMostRecentInvitation(eventCollectionId, mail
							.getInvitation().getObmUID(), mail.getInvitation()
							.getDtStamp())) {
						logger
								.info("A more recent event or email is synchronized on phone. The email[UID: "
										+ mail.getUid()
										+ "dtstam: "
										+ mail.getInvitation().getDtStamp()
										+ "] will not synced");
						it.remove();
					} else {
						storage.markToDeletedSyncedInvitation(
								eventCollectionId, mail.getInvitation()
										.getObmUID());
						System.err.println("MARK TO DELETES SYNCED INVITATION");
						Boolean update = storage.haveEventToDeleted(
								eventCollectionId, mail.getInvitation()
										.getObmUID());
						System.err.println("haveEventToDELETE: "+update);
						if (update) {
							storage.createOrUpdateInvitation(eventCollectionId,
									mail.getInvitation().getObmUID(),
									emailCollectionId, mail.getUid(), mail
											.getInvitation().getDtStamp(),
									InvitationStatus.EMAIL_TO_SYNCED, null);
							it.remove();
							System.err.println("it.remove: ");
						} else {
							storage.createOrUpdateInvitation(eventCollectionId,
									mail.getInvitation().getObmUID(),
									emailCollectionId, mail.getUid(), mail
											.getInvitation().getDtStamp(),
									InvitationStatus.EMAIL_SYNCED, state
											.getKey());
						}
					}
				}
			}

			List<Long> emailToDeleted = storage.getEmailToDeleted(
					emailCollectionId, state.getKey());
			logger.info(emailToDeleted.size() + " email(s) will be deleted on the PDA");
			List<ItemChange> itemsToDeleted = this.getDeletions(
					emailCollectionId, emailToDeleted);
			storage.updateInvitationStatus(InvitationStatus.DELETED, state
					.getKey(), eventCollectionId, emailCollectionId,
					emailToDeleted.toArray(new Long[0]));
			delta.getDeletions().addAll(itemsToDeleted);
		} catch (ActiveSyncException e) {
			logger.info(e.getMessage(), e);
		}
	}

	public List<ItemChange> fetchItems(BackendSession bs, List<String> fetchIds)
			throws ActiveSyncException {
		LinkedList<ItemChange> ret = new LinkedList<ItemChange>();
		for (String serverId : fetchIds) {
			Integer collectionId = getCollectionIdFor(serverId);
			String collectionPath = getCollectionPathFor(collectionId);
			Set<Long> uids = new HashSet<Long>();
			uids.add(getItemIdFor(serverId).longValue());
			List<MSEmail> emails;
			try {
				emails = emailManager.fetchMails(bs, getCalendarClient(bs),
						collectionId, collectionPath, uids);
				if (emails.size() > 0) {
					MSEmail email = emails.get(0);
					ItemChange ic = new ItemChange();
					ic.setServerId(getServerIdFor(collectionId, ""
							+ email.getUid()));
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
		if (moveToTrash) {
			logger.info("move to trash serverId " + serverId);
		} else {
			logger.info("delete serverId " + serverId);
		}
		if (serverId != null) {
			try {
				Long uid = getItemIdFor(serverId).longValue();
				Integer collectionId = getCollectionIdFor(serverId);
				String collectionName = getCollectionPathFor(collectionId);
				Integer devId = getDevId(bs.getDevId());

				if (moveToTrash) {
					String wasteBasketPath = getWasteBasketPath(bs);
					Integer wasteBasketId = getCollectionIdFor(bs.getDevId(),
							wasteBasketPath);
					emailManager.moveItem(bs, devId, collectionName,
							collectionId, wasteBasketPath, wasteBasketId, uid);
				} else {
					emailManager.delete(bs, devId, collectionName,
							collectionId, uid);
				}
				removeInvitationStatus(bs, collectionId, uid);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void removeInvitationStatus(BackendSession bs,
			Integer emailCollectionId, Long mailUid) {
		try {
			String calPath = getDefaultCalendarName(bs);
			Integer eventCollectionId = getCollectionIdFor(bs.getDevId(),
					calPath);
			storage.removeInvitationStatus(eventCollectionId,
					emailCollectionId, mailUid);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

	}

	public String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, String clientId, MSEmail data)
			throws ActiveSyncException {
		String collectionPath = getCollectionPathFor(collectionId);
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", "
				+ collectionPath + ", " + serverId + ", " + clientId + ")");
		if (serverId != null) {
			Long mailUid = getItemIdFor(serverId).longValue();
			try {
				emailManager.updateReadFlag(bs, collectionPath, mailUid, data
						.isRead());
			} catch (IMAPException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return null;
	}

	public String move(BackendSession bs, String srcFolder, String dstFolder,
			String messageId) throws ServerErrorException {
		logger.info("move(" + bs.getLoginAtDomain() + ", messageId "
				+ messageId + " from " + srcFolder + " to " + dstFolder + ")");
		Integer srcFolderId = null;
		Integer dstFolderId = null;
		Long newUidMail = null;
		try {
			Long currentMailUid = getItemIdFor(messageId).longValue();
			srcFolderId = getCollectionIdFor(bs.getDevId(), srcFolder);
			dstFolderId = getCollectionIdFor(bs.getDevId(), dstFolder);
			Integer devId = getDevId(bs.getDevId());

			newUidMail = emailManager.moveItem(bs, devId, srcFolder,
					srcFolderId, dstFolder, dstFolderId, currentMailUid);
			removeInvitationStatus(bs, srcFolderId, currentMailUid);
			return dstFolderId + ":" + newUidMail;
		} catch (Exception e) {
			throw new ServerErrorException(e);
		}

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
			Boolean saveInSent, Integer collectionId, String serverId) {
		try {
			String collectionPath = getCollectionPathFor(collectionId);
			Long uid = getItemIdFor(serverId).longValue();
			Set<Long> uids = new HashSet<Long>();
			uids.add(uid);
			List<MSEmail> mail = emailManager.fetchMails(bs,
					getCalendarClient(bs), collectionId, collectionPath, uids);

			if (mail.size() > 0) {
				ReplyEmailHandler reh = new ReplyEmailHandler(getUserEmail(bs),
						mail.get(0));
				send(bs, mailContent, reh, saveInSent);
				emailManager.setAnsweredFlag(bs, collectionPath, uid);
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
			String collectionName = getCollectionPathFor(Integer
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
		byte[] emailData = FileUtils.streamBytes( handler.getMessage(), true);
		InputStream email = null;
		try {
			EmailConverter conv = new EmailConverter();
			email = conv.convert(new ByteArrayInputStream(emailData));
		} catch (Throwable e) {
			logger.info(e.getMessage(), e);
		}
		if(email == null){
			email = new ByteArrayInputStream(emailData);
		}
		emailManager.sendEmail(bs, handler.getFrom(), handler.getTo(), email,
				saveInSent);
	}

	public MSEmail getEmail(BackendSession bs, Integer collectionId,
			String serverId) throws ActiveSyncException {
		String collectionName = getCollectionPathFor(collectionId);
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

				String collectionName = getCollectionPathFor(Integer
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

	public void purgeFolder(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws CollectionNotFoundException,
			NotAllowedException {
		String wasteBasketPath = getWasteBasketPath(bs);
		if (!wasteBasketPath.equals(collectionPath)) {
			throw new NotAllowedException(
					"Only the Trash folder can be purged.");
		}
		try {

			int devId = getDevId(bs.getDevId());
			int collectionId = getCollectionIdFor(bs.getDevId(), collectionPath);
			emailManager.purgeFolder(bs, devId, collectionPath, collectionId);
			if (deleteSubFolder) {
				logger
						.warn("deleteSubFolder isn't implemented because opush doesn't yet manage folders");
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new NotAllowedException(e);
		}
	}
}
