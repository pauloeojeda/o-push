package org.obm.push.backend.obm22;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.MSContact;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.PIMDataType;
import org.obm.push.backend.obm22.calendar.CalendarBackend;
import org.obm.push.backend.obm22.contacts.ContactsBackend;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.NotAllowedException;

/**
 * 
 * @author adrienp
 * 
 */
public class ContentsImporter implements IContentsImporter {

	private MailBackend mailBackend;
	private CalendarBackend calBackend;
	private ContactsBackend contactBackend;

	// private TasksBackend tasksBackend;
	@SuppressWarnings("unused")
	private Log logger = LogFactory.getLog(getClass());

	public ContentsImporter(MailBackend mailBackend,
			CalendarBackend calBackend, ContactsBackend contactBackend) {
		this.mailBackend = mailBackend;
		this.calBackend = calBackend;
		this.contactBackend = contactBackend;
		// this.tasksBackend = tasksBackend;
	}

	@Override
	public String importMessageChange(BackendSession bs, String collectionId,
			String serverId, String clientId, IApplicationData data)
			throws ActiveSyncException {
		String id = null;
		switch (data.getType()) {
		case CALENDAR:
			id = calBackend.createOrUpdate(bs, collectionId, serverId,
					clientId, data);
			break;
		case CONTACTS:
			id = contactBackend.createOrUpdate(bs, collectionId, serverId,
					clientId, (MSContact) data);
			break;
		case EMAIL:
			id = mailBackend.createOrUpdate(bs, collectionId, serverId,
					clientId, (MSEmail) data);
			break;
		case TASKS:
			id = calBackend.createOrUpdate(bs, collectionId, serverId,
					clientId, data);
			break;
		}
		return id;
	}

	@Override
	public void importMessageDeletion(BackendSession bs, PIMDataType type,
			String collectionId, String serverId) {
		switch (type) {
		case CALENDAR:
			calBackend.delete(bs, collectionId, serverId);
			break;
		case CONTACTS:
			contactBackend.delete(bs, serverId);
			break;
		case EMAIL:
			mailBackend.delete(bs, serverId);
			break;
		case TASKS:
			calBackend.delete(bs, collectionId, serverId);
			break;
		}
	}

	@Override
	public void importMessageMove(BackendSession bs, String serverId,
			String trash) {

	}

	public String importMoveItem(BackendSession bs, PIMDataType type,
			String srcFolder, String dstFolder, String messageId) {
		switch (type) {
		case CALENDAR:
			break;
		case CONTACTS:
			break;
		case EMAIL:
			return mailBackend.move(bs, srcFolder, dstFolder, messageId);
		case TASKS:
			break;
		}
		return null;
	}

	@Override
	public void sendEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent) {
		mailBackend.sendEmail(bs, mailContent, saveInSent);
	}

	@Override
	public void replyEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent, String collectionId, String serverId) {
		mailBackend.replyEmail(bs, mailContent, saveInSent, collectionId,
				serverId);
	}

	@Override
	public String importCalendarUserStatus(BackendSession bs, MSEvent event,
			AttendeeStatus userResponse) {
		return calBackend.updateUserStatus(bs, event, userResponse);
	}

	@Override
	public void forwardEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent, String collectionId, String serverId) {
		mailBackend.forwardEmail(bs, mailContent, saveInSent, collectionId,
				serverId);
	}

	@Override
	public void emptyFolderContent(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws CollectionNotFoundException,
			NotAllowedException {
		if (collectionPath != null && collectionPath.contains("email\\")) {
			mailBackend.purgeFolder(bs, collectionPath, deleteSubFolder);
		} else {
			throw new NotAllowedException(
					"emptyFolderContent is only supported for emails, collection was "
							+ collectionPath);
		}

	}

}
