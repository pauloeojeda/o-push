package org.obm.push.backend;

import org.obm.push.state.SyncState;

public interface IImporter {

	ServerId importFolderChange(SyncFolder sf);
	
	ServerId importFolderDeletion(SyncFolder sf);

	void configure(SyncState state);
	
	
}
