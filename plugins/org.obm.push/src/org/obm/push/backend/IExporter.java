package org.obm.push.backend;

import org.obm.push.state.SyncState;

public interface IExporter {

	void configure(IImporter imem, String dataClass, Integer filterType,
			SyncState state, int i, int j);

	SyncState getState();

	Integer getChangesCount();

	void synchronize();

}
