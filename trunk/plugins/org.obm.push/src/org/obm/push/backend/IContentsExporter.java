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

	void configure(BackendSession bs, String dataClass, Integer filterType, SyncState state, String collectionId);

	SyncState getState(BackendSession bs);

	DataDelta getChanged(BackendSession bs, String collectionId);

	int getCount(BackendSession bs, String collectionId);

	List<ItemChange> fetch(BackendSession bs, List<String> fetchIds);
	
	Integer getDefaultCalendarId(BackendSession bs);
}
