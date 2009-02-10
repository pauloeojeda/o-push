package org.obm.push.backend.obm22;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IExporter;
import org.obm.push.backend.IImporter;

public class OBMBackend implements IBackend {

	private IImporter importer;
	private IExporter exporter;

	public OBMBackend() {
		importer = new Importer();
		exporter = new Exporter();
	}

	@Override
	public IImporter getHierarchyImporter() {
		return importer;
	}

	@Override
	public IExporter getExporter() {
		return exporter;
	}

}
