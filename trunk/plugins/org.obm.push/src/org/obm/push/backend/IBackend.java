package org.obm.push.backend;

import org.obm.push.provisioning.Policy;

public interface IBackend {

	IHierarchyImporter getHierarchyImporter();
	
	IHierarchyExporter getHierarchyExporter();

	IContentsImporter getContentsImporter(String string);

	IContentsExporter getContentsExporter();

	String getWasteBasket();
	
	Policy getDevicePolicy();
	
}
