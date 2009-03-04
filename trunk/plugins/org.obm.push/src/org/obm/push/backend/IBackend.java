package org.obm.push.backend;

public interface IBackend {

	IHierarchyImporter getHierarchyImporter();
	
	IHierarchyExporter getHierarchyExporter();

	IContentsImporter getContentsImporter(String string);

	IContentsExporter getContentsExporter();

	String getWasteBasket();
	
}
