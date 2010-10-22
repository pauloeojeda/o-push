package org.obm.push.backend;

import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.NotAllowedException;
import org.obm.push.exception.ServerErrorException;

/**
 * Content management interface, ie. CRUD API.
 * 
 * @author tom
 * 
 */
public interface IContentsImporter {

	String importMessageChange(BackendSession bs, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws ActiveSyncException;

	void importMessageDeletion(BackendSession bs, PIMDataType type,
			String collectionId, String serverId, Boolean moveToTrash);

	String importMoveItem(BackendSession bs, PIMDataType type,
			String srcFolder, String dstFolder, String messageId)
			throws ServerErrorException;

	void sendEmail(BackendSession bs, byte[] mailContent, Boolean saveInSent);

	void replyEmail(BackendSession bs, byte[] mailContent, Boolean saveInSent,
			Integer collectionId, String serverId);

	String importCalendarUserStatus(BackendSession bs, MSEvent invi,
			AttendeeStatus userResponse);

	void forwardEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent, String collectionId, String serverId);

	void emptyFolderContent(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws CollectionNotFoundException,
			NotAllowedException;

}
