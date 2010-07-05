package org.obm.push.backend;

public class MSTask implements IApplicationData {

	@Override
	public PIMDataType getType() {
		return PIMDataType.TASKS;
	}

	@Override
	public boolean isRead() {
		return false;
	}
}
