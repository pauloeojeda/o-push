package org.obm.push.backend;

public class BackendSession {

	private String loginAtDomain;
	private String password;
	private String devId;
	private String devType;
	private String command;

	public BackendSession(String loginAtDomain, String password, String devId, String devType, String command) {
		super();
		this.loginAtDomain = loginAtDomain;
		this.password = password;
		this.devId = devId;
		this.devType = devType;
		this.command = command;
	}

	public String getLoginAtDomain() {
		return loginAtDomain;
	}

	public void setLoginAtDomain(String loginAtDomain) {
		this.loginAtDomain = loginAtDomain;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDevId() {
		return devId;
	}

	public void setDevId(String devId) {
		this.devId = devId;
	}

	public String getDevType() {
		return devType;
	}

	public void setDevType(String devType) {
		this.devType = devType;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
