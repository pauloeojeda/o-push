package org.obm.push.backend;

import org.obm.push.PushConfiguration;

public interface IBackendFactory {

	IBackend loadBackend(PushConfiguration pc);

}
