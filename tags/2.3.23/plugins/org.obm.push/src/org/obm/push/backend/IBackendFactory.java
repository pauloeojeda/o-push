package org.obm.push.backend;

import org.obm.push.PushConfiguration;
import org.obm.push.store.ISyncStorage;

public interface IBackendFactory {

	IBackend loadBackend(ISyncStorage storage, PushConfiguration pc);

}
