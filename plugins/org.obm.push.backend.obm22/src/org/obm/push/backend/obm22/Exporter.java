package org.obm.push.backend.obm22;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.IExporter;
import org.obm.push.backend.ImportHierarchyChangesMem;
import org.obm.push.state.SyncState;

public class Exporter implements IExporter {

	private static final Log logger = LogFactory.getLog(Exporter.class);

	@Override
	public void configure(ImportHierarchyChangesMem imem, boolean b, boolean c,
			SyncState state, int i, int j) {
		// TODO Auto-generated method stub
		logger.info("configure(imem, " + b + ", " + c + ", " + state + ", " + i
				+ ", " + j + ")");
	}

	@Override
	public SyncState getState() {
		// TODO Auto-generated method stub
		return null;
	}

}
