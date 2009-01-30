package org.obm.push.impl;

public class ASParams {

	private String userId;
	private String devId;
	private String devType;
	private String command;

	public ASParams(String userId, String devId, String devType, String command) {
		super();
		this.userId = userId;
		this.devId = devId;
		this.devType = devType;
		this.command = command;
	}

	public String getUserId() {
		return userId;
	}

	public String getDevId() {
		return devId;
	}

	public String getDevType() {
		return devType;
	}

	public String getCommand() {
		return command;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(200);
		sb.append("u: ");
		sb.append(userId);
		sb.append(" d: ");
		sb.append(devId);
		sb.append(" t: ");
		sb.append(devType);
		sb.append(" c: ");
		sb.append(command);
		return sb.toString();
	}
	
	

}
