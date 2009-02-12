package org.obm.push.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obm.push.state.SyncState;

public class SyncCollection {
	
	private SyncState syncState;
	private boolean importedChanges;
	private Map<String, String> clientIds;
	private List<String> fetchIds;
	private String dataClass;
	private Integer conflict;
	private String collectionId;
	private String syncKey;
	private Integer truncation;
	private boolean deletesAsMoves;
	private String newSyncKey;
	
	public SyncCollection() {
		clientIds = new HashMap<String, String>();
		fetchIds = new LinkedList<String>();
		conflict = 1;
		truncation = SyncHandler.SYNC_TRUNCATION_ALL;
	}
	
	public SyncState getSyncState() {
		return syncState;
	}
	public void setSyncState(SyncState syncState) {
		this.syncState = syncState;
	}
	public boolean isImportedChanges() {
		return importedChanges;
	}
	public void setImportedChanges(boolean importedChanges) {
		this.importedChanges = importedChanges;
	}
	public Map<String, String> getClientIds() {
		return clientIds;
	}
	public void setClientIds(Map<String, String> clientIds) {
		this.clientIds = clientIds;
	}
	public String getDataClass() {
		return dataClass;
	}
	public void setDataClass(String dataClass) {
		this.dataClass = dataClass;
	}
	public Integer getConflict() {
		return conflict;
	}
	public void setConflict(Integer conflict) {
		this.conflict = conflict;
	}
	public String getCollectionId() {
		return collectionId;
	}
	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}
	public String getSyncKey() {
		return syncKey;
	}
	public void setSyncKey(String syncKey) {
		this.syncKey = syncKey;
	}

	public Integer getTruncation() {
		return truncation;
	}

	public void setTruncation(Integer truncation) {
		this.truncation = truncation;
	}

	public boolean isDeletesAsMoves() {
		return deletesAsMoves;
	}

	public void setDeletesAsMoves(boolean deletesAsMoves) {
		this.deletesAsMoves = deletesAsMoves;
	}

	public List<String> getFetchIds() {
		return fetchIds;
	}

	public void setFetchIds(List<String> fetchIds) {
		this.fetchIds = fetchIds;
	}

	public String getNewSyncKey() {
		return newSyncKey;
	}

	public void setNewSyncKey(String newSyncKey) {
		this.newSyncKey = newSyncKey;
	}

}
