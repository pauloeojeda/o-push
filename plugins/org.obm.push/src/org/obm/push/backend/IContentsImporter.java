package org.obm.push.backend;

import org.obm.push.state.SyncState;

public interface IContentsImporter {

	void configure(SyncState syncState, Integer conflictPolicy);

	void importMessageReadFlag(String serverId, boolean read);

	String importMessageChange(String serverId, IApplicationData data);

	void importMessageMove(String serverId, String trash);

	void importMessageDeletion(String serverId);

	SyncState getState();

}
