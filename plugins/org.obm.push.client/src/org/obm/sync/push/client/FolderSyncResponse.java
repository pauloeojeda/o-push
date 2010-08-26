package org.obm.sync.push.client;

import java.util.List;

public class FolderSyncResponse implements IEasReponse {

	private List<Folder> fl;
	private String key;

	public FolderSyncResponse(String key, List<Folder> fl) {
		this.fl = fl;
		this.key = key;
	}
	
	@Override
	public String getReturnedSyncKey() {
		return key;
	}

	public List<Folder> getFolders() {
		return fl;
	}
	
}
