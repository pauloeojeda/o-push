package org.obm.push.backend;

import java.util.List;

import org.obm.push.state.SyncState;

public interface IExporter {

	void configure(String dataClass, Integer filterType, SyncState state,
			int i, int j);

	SyncState getState();

	Integer getChangesCount();

	void synchronize();

	List<ItemChange> getChanged();

}
