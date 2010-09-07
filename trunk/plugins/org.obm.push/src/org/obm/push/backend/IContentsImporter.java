package org.obm.push.backend;

import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.NotAllowedException;

/**
 * Content management interface, ie. CRUD API.
 * 
 * @author tom
 * 
 */
public interface IContentsImporter {

	void importMessageReadFlag(BackendSession bs, String serverId, boolean read);

	String importMessageChange(BackendSession bs, String collectionId,
			String serverId, String clientId, IApplicationData data)
			throws ActiveSyncException;

	void importMessageMove(BackendSession bs, String serverId, String trash);

	void importMessageDeletion(BackendSession bs, PIMDataType type,
			String collectionId, String serverId);

	String importMoveItem(BackendSession bs, PIMDataType type,
			String srcFolder, String dstFolder, String messageId);

	void sendEmail(BackendSession bs, byte[] mailContent, Boolean saveInSent);

	void replyEmail(BackendSession bs, byte[] mailContent, Boolean saveInSent,
			String collectionId, String serverId);

	String importCalendarUserStatus(BackendSession bs, MSEvent invi,
			AttendeeStatus userResponse);

	void forwardEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent, String collectionId, String serverId);

	void emptyFolderContent(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws CollectionNotFoundException,
			NotAllowedException;

}
