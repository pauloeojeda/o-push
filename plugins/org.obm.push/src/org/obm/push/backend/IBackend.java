package org.obm.push.backend;

public interface IBackend {

	IImporter getHierarchyImporter();
	
	IExporter getExporter();
	
}
