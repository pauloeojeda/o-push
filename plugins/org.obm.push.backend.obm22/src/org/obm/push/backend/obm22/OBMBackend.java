package org.obm.push.backend.obm22;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IImporter;

public class OBMBackend implements IBackend {

	private IImporter importer;
	
	public OBMBackend() {
		importer = new Importer(); 
	}
	
	@Override
	public IImporter getHierarchyImporter() {
		return importer;
	}

}
