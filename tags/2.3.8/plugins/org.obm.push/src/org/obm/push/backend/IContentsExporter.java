package org.obm.push.backend;

import java.util.List;

import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.state.SyncState;

/**
 * The exporter API fetches data from the backend store and returns it to the
 * mobile device
 * 
 * @author tom
 * 
 */
public interface IContentsExporter {

	void configure(String dataClass, FilterType filterType,
			SyncState state, String collectionId);

//	SyncState getState(BackendSession bs);

	DataDelta getChanged(BackendSession bs, SyncState state, FilterType filterType,
			String collectionId);

	int getCount(BackendSession bs, SyncState state, FilterType filterType, String collectionId);

	List<ItemChange> fetch(BackendSession bs, PIMDataType getDataType, List<String> fetchIds)
			throws ActiveSyncException;

	MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentName) throws ObjectNotFoundException;

	boolean validatePassword(String userID, String password);

	public List<ItemChange> fetchMails(BackendSession bs, List<String> fetchServerIds)
			throws ActiveSyncException;

}
