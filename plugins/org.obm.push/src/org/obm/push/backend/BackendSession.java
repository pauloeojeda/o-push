package org.obm.push.backend;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	private Map<Integer,Date> updatedSyncDate;
	private Map<Integer, Set<ItemChange>> unSynchronizedItemChangeByCollection;
	private Map<Integer, SyncState> lastClientSyncState;

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
		this.unSynchronizedItemChangeByCollection = new HashMap<Integer, Set<ItemChange>>();
		this.lastClientSyncState = new HashMap<Integer, SyncState>();
		this.updatedSyncDate = new HashMap<Integer, Date>();
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

	public Date getUpdatedSyncDate(Integer collectionId) {
		return updatedSyncDate.get(collectionId);
	}

	public void addUpdatedSyncDate(Integer collectionId, Date updatedSyncDate) {
		this.updatedSyncDate.put(collectionId, updatedSyncDate);
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

	public Set<ItemChange> getUnSynchronizedItemChange(Integer collectionId) {
		Set<ItemChange> ret = unSynchronizedItemChangeByCollection.get(collectionId);
		if(ret == null){
			ret = new HashSet<ItemChange>();
		}
		return ret;
	}

	public void addUnSynchronizedItemChange(Integer collectionId, ItemChange change ) {
		Set<ItemChange> changes = unSynchronizedItemChangeByCollection.get(collectionId);
		if(changes == null){
			changes = new HashSet<ItemChange>();
			unSynchronizedItemChangeByCollection.put(collectionId, changes);
		}
		changes.add(change);
	}
	
	public SyncState getLastClientSyncState(Integer collectionId) {
		return lastClientSyncState.get(collectionId);
	}

	public void addLastClientSyncState(Integer collectionId, SyncState synckey) {
		lastClientSyncState.put(collectionId, synckey);
	}

	public void clearUpdatedSyncDate() {
		
	}

	public void clearUnSynchronizedItemChangeByCollection() {
		
	}

	public void clearLastClientSyncState() {
		
	}
	
	public void clearAll(){
		this.updatedSyncDate = new HashMap<Integer, Date>();
		this.unSynchronizedItemChangeByCollection = new HashMap<Integer, Set<ItemChange>>();
		this.lastClientSyncState = new HashMap<Integer, SyncState>();
	}
	
	public void clear(Integer collectionId){
		this.updatedSyncDate.remove(collectionId);
		this.unSynchronizedItemChangeByCollection.remove(collectionId);
		this.lastClientSyncState.remove(collectionId);
	}
}
