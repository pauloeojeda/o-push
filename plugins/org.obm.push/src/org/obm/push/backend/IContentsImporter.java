package org.obm.push.backend;

import org.obm.push.state.SyncState;

public interface IContentsImporter {

	void configure(BackendSession bs, SyncState syncState,
			Integer conflictPolicy);

	void importMessageReadFlag(BackendSession bs, String serverId, boolean read);

	String importMessageChange(BackendSession bs, String collectionId,
			String serverId, String clientId, IApplicationData data);

	void importMessageMove(BackendSession bs, String serverId, String trash);

	void importMessageDeletion(BackendSession bs, PIMDataType type,
			String serverId);

	SyncState getState(BackendSession bs);

}
