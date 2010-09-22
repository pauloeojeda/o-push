package org.obm.push.backend.obm22.mail;

import java.sql.SQLException;

import org.minig.imap.StoreClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.state.SyncState;

public interface IEmailSync {
	void addMessage(Integer devId, Integer collectionId, Long mailUid);

	void deleteMessage(Integer devId, Integer collectionId, Long mailUid);

	MailChanges getSync(StoreClient imapStore, Integer devId,
			BackendSession bs, SyncState state, Integer collectionId,
			String mailBox) throws InterruptedException, SQLException;
}
