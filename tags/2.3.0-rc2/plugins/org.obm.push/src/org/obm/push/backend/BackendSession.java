package org.obm.push.backend;

import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
	private Date updatedSyncDate;
	private List<ItemChange> unSynchronizedItemChange;

	private double protocolVersion;

	private String policyKey;

	private long lastHeartbeat;
	private Set<SyncCollection> lastMonitored;
	
	public BackendSession(String loginAtDomain, String password, String devId,
			String devType, String command) {
		super();
		this.loginAtDomain = loginAtDomain;
		this.password = password;
		this.devId = devId;
		this.devType = devType;
		this.command = command;
		this.unSynchronizedItemChange = new LinkedList<ItemChange>();
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
			InputStream in = BackendSession.class.getClassLoader()
			.getResourceAsStream("hints/" + devType + ".hints");
			hints.load(in);
			in.close();
			logger.info("Loaded hints for "+devType);
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

	public long getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}

	public Set<SyncCollection> getLastMonitored() {
		return lastMonitored;
	}

	public void setLastMonitored(Set<SyncCollection> lastMonitored) {
		this.lastMonitored = lastMonitored;
	}
	
	public List<ItemChange> getUnSynchronizedItemChange() {
		return unSynchronizedItemChange;
	}

	public void addUnSynchronizedItemChange(ItemChange itemChange) {
		this.unSynchronizedItemChange.add(itemChange);
	}

}