package org.obm.push.backend;

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

	void sendMail(BackendSession bs, byte[] mailContent);
	
	
	/**
	 * Push support
	 * 
	 * @param ccl
	 * @return a registration that the caller can use to cancel monitor of a ressource
	 */
	IListenerRegistration addChangeListener(ICollectionChangeListener ccl);

	void resetForFullSync(String devId);

}
