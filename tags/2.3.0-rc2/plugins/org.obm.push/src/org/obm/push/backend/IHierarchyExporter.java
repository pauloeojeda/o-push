package org.obm.push.backend;

import java.util.List;

import org.obm.push.state.SyncState;

/**
 * The exporter API fetches data from the backend store and returns it to the
 * mobile device
 * 
 * @author tom
 * 
 */
public interface IHierarchyExporter {

	void configure(BackendSession bs, String dataClass, Integer filterType, SyncState state,
			int i, int j);

	SyncState getState(BackendSession bs);

	void synchronize(BackendSession bs);

	List<ItemChange> getChanged(BackendSession bs);

	int getCount(BackendSession bs);

	List<ItemChange> getDeleted(BackendSession bs);
	
	int getRootFolderId(BackendSession bs);

}
