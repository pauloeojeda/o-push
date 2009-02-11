package org.obm.push.backend;

public interface IBackend {

	IHierarchyImporter getHierarchyImporter();
	
	IExporter getExporter();

	IContentsImporter getContentsImporter(String string);

	String getWasteBasket();
	
}
