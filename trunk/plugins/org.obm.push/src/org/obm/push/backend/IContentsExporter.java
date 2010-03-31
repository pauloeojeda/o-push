package org.obm.push.backend;

import java.util.List;

import org.obm.push.exception.ActiveSyncException;
import org.obm.push.state.SyncState;

/**
 * The exporter API fetches data from the backend store and returns it to the
 * mobile device
 * 
 * @author tom
 * 
 */
public interface IContentsExporter {

	void configure(BackendSession bs, String dataClass, FilterType filterType,
			SyncState state, String collectionId);

	SyncState getState(BackendSession bs);

	DataDelta getChanged(BackendSession bs, FilterType filterType,
			String collectionId);

	int getCount(BackendSession bs, FilterType filterType, String collectionId);

	List<ItemChange> fetch(BackendSession bs, List<String> fetchIds)
			throws ActiveSyncException;

	MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentName);

	boolean validatePassword(String userID, String password);

	List<SearchResult> search(BackendSession bs, StoreName storeName, String query,
			Integer rangeLower, Integer rangeUpper);
}
