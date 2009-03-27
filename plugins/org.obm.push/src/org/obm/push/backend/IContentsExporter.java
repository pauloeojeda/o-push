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
public interface IContentsExporter {

	void configure(BackendSession bs, String dataClass, Integer filterType, SyncState state,
			int i, int j);

	SyncState getState(BackendSession bs);

	List<ItemChange> getChanged(BackendSession bs, String collectionId);

	int getCount(BackendSession bs, String collectionId);

	List<ItemChange> getDeleted(BackendSession bs, String collectionId);

	List<ItemChange> fetch(BackendSession bs, List<String> fetchIds);

}