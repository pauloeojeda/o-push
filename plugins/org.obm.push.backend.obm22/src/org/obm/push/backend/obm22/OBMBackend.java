package org.obm.push.backend.obm22;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.IHierarchyImporter;

public class OBMBackend implements IBackend {

	private IHierarchyImporter hImporter;
	private IContentsImporter cImporter;
	private IHierarchyExporter exporter;
	private IContentsExporter contentsExporter;

	public OBMBackend() {
		hImporter = new HierarchyImporter();
		exporter = new HierarchyExporter();
		cImporter = new ContentsImporter();
		contentsExporter = new ContentsExporter();
	}

	@Override
	public IHierarchyImporter getHierarchyImporter() {
		return hImporter;
	}

	@Override
	public IHierarchyExporter getHierarchyExporter() {
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

	@Override
	public IContentsExporter getContentsExporter() {
		return contentsExporter;
	}

}
