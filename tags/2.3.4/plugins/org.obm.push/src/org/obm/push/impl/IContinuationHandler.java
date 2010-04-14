package org.obm.push.impl;

import java.util.Set;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.SyncCollection;

public interface IContinuationHandler {
	void sendResponse(BackendSession bs, Responder responder,
			Set<SyncCollection> changedFolders, boolean sendHierarchyChange);
	
	void sendError(Responder responder,
			Set<SyncCollection> changedFolders, String errorStatus);
}
