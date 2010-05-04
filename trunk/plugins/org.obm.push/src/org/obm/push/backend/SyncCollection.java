package org.obm.push.backend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.obm.push.impl.SyncHandler;
import org.obm.push.state.SyncState;

public class SyncCollection {
	
	private SyncState syncState;
	private List<String> fetchIds;
	private String dataClass;
	private Integer conflict;
	private Integer collectionId;
	private String collectionPath;
	private String syncKey;
	private Integer truncation;
	private boolean deletesAsMoves;
	private FilterType filterType;
	private Integer windowSize;
	private boolean moreAvailable;
	private Integer mimeSupport;
	private Integer mimeTruncation;
	private Map<MSEmailBodyType,BodyPreference> bodyPreferences;
	
	
	public SyncCollection() {
		fetchIds = new LinkedList<String>();
		conflict = 1;
		collectionId = 0;
		truncation = SyncHandler.SYNC_TRUNCATION_ALL;
		moreAvailable = false;
		windowSize = 100;
		this.bodyPreferences = new HashMap<MSEmailBodyType, BodyPreference>();
	}
	
	public SyncState getSyncState() {
		return syncState;
	}
	public void setSyncState(SyncState syncState) {
		this.syncState = syncState;
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
	public Integer getCollectionId() {
		return collectionId;
	}
	public void setCollectionId(Integer collectionId) {
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

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}
	
	public Integer getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(Integer windowSize) {
		this.windowSize = windowSize;
	}
	
	public boolean isMoreAvailable() {
		return moreAvailable;
	}

	public void setMoreAvailable(boolean moreAvailable) {
		this.moreAvailable = moreAvailable;
	}
	
	public String getCollectionPath() {
		return collectionPath;
	}

	public void setCollectionPath(String collectionPath) {
		this.collectionPath = collectionPath;
	}

	@Override
	public boolean equals(Object obj) {
		return collectionId.equals(((SyncCollection) obj).collectionId);
	}

	@Override
	public int hashCode() {
		return collectionId.hashCode();
	}

	public Integer getMimeSupport() {
		return mimeSupport;
	}

	public void setMimeSupport(Integer mimeSupport) {
		this.mimeSupport = mimeSupport;
	}

	public Integer getMimeTruncation() {
		return mimeTruncation;
	}

	public void setMimeTruncation(Integer mimeTruncation) {
		this.mimeTruncation = mimeTruncation;
	}
	
	public Map<MSEmailBodyType,BodyPreference> getBodyPreferences() {
		return bodyPreferences;
	}
	
	public BodyPreference getBodyPreference(MSEmailBodyType type) {
		return bodyPreferences.get(type);
	}

	public void addBodyPreference(BodyPreference bodyPreference) {
		this.bodyPreferences.put(bodyPreference.getType(), bodyPreference);
	}
	
}
