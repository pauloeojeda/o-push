package org.obm.push.backend.obm22;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.PushConfiguration;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IBackendFactory;

public class BackendFactory implements IBackendFactory {

	private static final Log logger = LogFactory.getLog(BackendFactory.class);

	@Override
	public IBackend loadBackend(PushConfiguration pc) {
		logger.info("Loading OBM 2.2.x backend...");
		return new OBMBackend();
	}

}
