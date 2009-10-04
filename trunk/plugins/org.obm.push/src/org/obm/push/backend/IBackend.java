package org.obm.push.backend;

import java.util.Set;

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

	IContentsImporter getContentsImporter(String collectionId, BackendSession bs);

	IContentsExporter getContentsExporter(BackendSession bs);

	String getWasteBasket();

	Policy getDevicePolicy(BackendSession bs);

	@Deprecated
	Set<SyncCollection> pollForChanges(IContinuation c, BackendSession bs,
			Set<SyncCollection> toMonitor, long msTimeout);

	ISyncStorage getStore();

	void sendMail(BackendSession bs, byte[] mailContent);
	
	
	/**
	 * Push support
	 * 
	 * @param ccl
	 * @return a registration that the caller can use to cancel monitor of a ressource
	 */
	IListenerRegistration addChangeListener(ICollectionChangeListener ccl);

}
