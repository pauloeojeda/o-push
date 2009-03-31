package org.obm.push.backend;

import java.util.Set;

import org.obm.push.provisioning.Policy;

public interface IBackend {

	IHierarchyImporter getHierarchyImporter(BackendSession bs);
	
	IHierarchyExporter getHierarchyExporter(BackendSession bs);

	IContentsImporter getContentsImporter(String collectionId, BackendSession bs);

	IContentsExporter getContentsExporter(BackendSession bs);

	String getWasteBasket();
	
	Policy getDevicePolicy(BackendSession bs);

	Set<SyncCollection> waitForChanges(BackendSession bs, Set<SyncCollection> toMonitor, long intervalSeconds);
	
}
