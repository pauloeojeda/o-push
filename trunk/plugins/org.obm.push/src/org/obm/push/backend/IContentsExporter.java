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

	void configure(String dataClass, Integer filterType, SyncState state,
			int i, int j);

	SyncState getState();

	void synchronize();

	List<ItemChange> getChanged();

	int getCount();

	List<ItemChange> getDeleted();

}
