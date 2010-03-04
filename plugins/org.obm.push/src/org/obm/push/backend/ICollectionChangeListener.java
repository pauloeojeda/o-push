package org.obm.push.backend;

import java.util.Set;

/**
 * This interface is used in the push process to wait for changes.
 * 
 * The backend will use the {@link IContinuation} to wake up the caller.
 * 
 * @author tom
 * 
 */
public interface ICollectionChangeListener {

	Set<SyncCollection> getMonitoredCollections();

	BackendSession getSession();

	IContinuation getContinuation();

	/**
	 * This method will be called when the {@link IContinuation} is waked up to
	 * find which monitored collections changed.
	 * 
	 * @return
	 */
	Set<SyncCollection> getDirtyCollections();

	/**
	 * Called by backend when a sync is needed.
	 * 
	 * @param dirtyCollections
	 */
	void changesDetected(Set<SyncCollection> dirtyCollections);

}
