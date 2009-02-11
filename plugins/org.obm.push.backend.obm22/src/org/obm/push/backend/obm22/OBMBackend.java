package org.obm.push.backend.obm22;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IExporter;
import org.obm.push.backend.IHierarchyImporter;

public class OBMBackend implements IBackend {

	private IHierarchyImporter hImporter;
	private IContentsImporter cImporter;
	private IExporter exporter;

	public OBMBackend() {
		hImporter = new Importer();
		exporter = new Exporter();
		cImporter = new ContentsImporter();
	}

	@Override
	public IHierarchyImporter getHierarchyImporter() {
		return hImporter;
	}

	@Override
	public IExporter getExporter() {
		return exporter;
	}

	@Override
	public IContentsImporter getContentsImporter(String collectionId) {
		return cImporter;
	}

	@Override
	public String getWasteBasket() {
		return "Trash";
	}

}
