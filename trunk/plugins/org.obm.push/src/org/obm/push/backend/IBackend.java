package org.obm.push.backend;

import java.util.Set;

import org.obm.push.provisioning.Policy;
import org.obm.push.store.ISyncStorage;

public interface IBackend {

	IHierarchyImporter getHierarchyImporter(BackendSession bs);

	IHierarchyExporter getHierarchyExporter(BackendSession bs);

	IContentsImporter getContentsImporter(String collectionId, BackendSession bs);

	IContentsExporter getContentsExporter(BackendSession bs);

	String getWasteBasket();

	Policy getDevicePolicy(BackendSession bs);

	Set<SyncCollection> pollForChanges(IContinuation c, BackendSession bs,
			Set<SyncCollection> toMonitor, long msTimeout);

	ISyncStorage getStore();

	void sendMail(BackendSession bs, byte[] mailContent);

}
