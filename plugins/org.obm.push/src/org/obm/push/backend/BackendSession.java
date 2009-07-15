package org.obm.push.backend;

import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.state.SyncState;

public class BackendSession {

	private static final Log logger = LogFactory.getLog(BackendSession.class);

	private String loginAtDomain;
	private String password;
	private String devId;
	private String devType;
	private String command;
	private SyncState state;
	private PIMDataType dataType;
	private Properties hints;
	private HttpServletRequest request;
	private Set<SyncCollection> changedFolders;
	private Date updatedSyncDate;

	private double protocolVersion;

	private String policyKey;

	public BackendSession(String loginAtDomain, String password, String devId,
			String devType, String command) {
		super();
		this.loginAtDomain = loginAtDomain;
		this.password = password;
		this.devId = devId;
		this.devType = devType;
		this.command = command;
		loadHints();
	}

	public void setHint(String key, boolean value) {
		hints.put(key, value);
	}

	public boolean checkHint(String key, boolean defaultValue) {
		if (!hints.containsKey(key)) {
			return defaultValue;
		} else {
			return "true".equals(hints.get(key));
		}
	}

	private void loadHints() {
		hints = new Properties();
		try {
			hints.load(BackendSession.class.getClassLoader()
					.getResourceAsStream("hints/" + devType + ".hints"));
		} catch (Throwable e) {
			logger.warn("could not load hints for device type " + devType);
		}
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

	public SyncState getState() {
		return state;
	}

	public void setState(SyncState state) {
		this.state = state;
	}

	public PIMDataType getDataType() {
		return dataType;
	}

	public void setDataType(PIMDataType dataType) {
		this.dataType = dataType;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public Set<SyncCollection> getChangedFolders() {
		return changedFolders;
	}

	public void setChangedFolders(Set<SyncCollection> changedFolders) {
		this.changedFolders = changedFolders;
	}

	public Date getUpdatedSyncDate() {
		return updatedSyncDate;
	}

	public void setUpdatedSyncDate(Date updatedSyncDate) {
		this.updatedSyncDate = updatedSyncDate;
	}

	public void setProtocolVersion(double parseInt) {
		this.protocolVersion = parseInt;
	}

	public double getProtocolVersion() {
		return protocolVersion;
	}

	public void setPolicyKey(String pKey) {
		this.policyKey = pKey;
	}

	public String getPolicyKey() {
		return policyKey;
	}

}
