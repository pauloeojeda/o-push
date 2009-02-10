package org.obm.push.backend;

import org.obm.push.state.SyncState;

public interface IExporter {

	void configure(ImportHierarchyChangesMem imem, boolean b, boolean c,
			SyncState state, int i, int j);

	SyncState getState();

}
