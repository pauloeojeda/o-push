package org.obm.push.backend;

import org.obm.push.exception.ActiveSyncException;
import org.obm.push.provisioning.Policy;
import org.obm.push.store.ISyncStorage;

/**
 * Main interface for o-push datasources
 * 
 * @author tom
 *
 */
public interface IBackend {

	IHierarchyImporter getHierarchyImporter(BackendSession bs);

	IHierarchyExporter getHierarchyExporter(BackendSession bs);

	IContentsImporter getContentsImporter(Integer collectionId, BackendSession bs);

	IContentsExporter getContentsExporter(BackendSession bs);

	String getWasteBasket();

	Policy getDevicePolicy(BackendSession bs);

	ISyncStorage getStore();

	/**
	 * Push support
	 * 
	 * @param ccl
	 * @return a registration that the caller can use to cancel monitor of a ressource
	 */
	IListenerRegistration addChangeListener(ICollectionChangeListener ccl);

	void resetForFullSync(BackendSession bs);
	
	void startEmailMonitoring(BackendSession bs, Integer collectionId) throws ActiveSyncException;

	void resetCollection(BackendSession bs, Integer collectionId);

	boolean validatePassword(String userID, String password);
}
