package org.obm.push.backend.obm22;

import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.state.SyncState;

public class ContentsImporter implements IContentsImporter {

	@Override
	public void configure(SyncState syncState, Integer conflictPolicy) {
		// TODO Auto-generated method stub

	}

	@Override
	public SyncState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String importMessageChange(String serverId, IApplicationData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importMessageDeletion(String serverId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void importMessageMove(String serverId, String trash) {
		// TODO Auto-generated method stub

	}

	@Override
	public void importMessageReadFlag(String serverId, boolean read) {
		// TODO Auto-generated method stub

	}

}
