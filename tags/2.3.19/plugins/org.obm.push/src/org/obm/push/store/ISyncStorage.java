package org.obm.push.store;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.push.backend.PIMDataType;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.state.SyncState;

public interface ISyncStorage {

	void updateState(String devId, Integer collectionId, SyncState oldState,
			SyncState state);

	SyncState findStateForDevice(String devId, Integer collectionId);

	SyncState findStateForKey(String syncKey);

	long findLastHearbeat(String devId);

	void updateLastHearbeat(String devId, long hearbeat);

	/**
	 * Stores device informations for the given user. Returns <code>true</code>
	 * if the device is allowed to synchronize.
	 * 
	 * @param loginAtDomain
	 * @param deviceId
	 * @param deviceType
	 * @return
	 */
	boolean initDevice(String loginAtDomain, String deviceId, String deviceType);

	Integer addCollectionMapping(String deviceId, String collection);

	/**
	 * Fetches the id associated with a given collection id string.
	 * 
	 * @param deviceId
	 * @param collectionId
	 * @return
	 */
	Integer getCollectionMapping(String deviceId, String collectionId)
			throws CollectionNotFoundException;

	String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException;

	PIMDataType getDataClass(String collectionId);

	void resetForFullSync(String devId);

	Integer getDevId(String deviceId);

	Set<Integer> getAllCollectionId(String devId);

	void resetCollection(String devId, Integer collectionId);

	/**
	 * Returns <code>true</code> if the device is authorized to synchronize.
	 * 
	 * @param login
	 * @param domain
	 * @param deviceId
	 * @return
	 */
	boolean syncAuthorized(String loginAtDomain, String deviceId);

	Boolean isMostRecentInvitation(Integer eventCollectionId, String eventUid,
			Date dtStamp);

	void markToDeletedSyncedInvitation(Integer eventCollectionId,
			String eventUid);
	
	Boolean haveEmailToDeleted(Integer eventCollectionId, String eventUid);
	
	Boolean haveEventToDeleted(Integer eventCollectionId, String eventUid);

	void createOrUpdateInvitation(Integer eventCollectionId,
			String eventUid, Date dtStamp, InvitationStatus status, String syncKey);

	void createOrUpdateInvitation(Integer eventCollectionId,
			String eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey);

	List<Long> getEmailToSynced(Integer emailCollectionId, String syncKey);

	List<Long> getEmailToDeleted(Integer emailCollectionId, String syncKey);

	List<String> getEventToSynced(Integer eventCollectionId, String syncKey);

	List<String> getEventToDeleted(Integer eventCollectionId, String syncKey);

	void updateInvitationStatus(InvitationStatus status, String syncKey, Integer eventCollectionId, Integer emailCollectionId,  Long... emailUid);
	
	void updateInvitationStatus(InvitationStatus status, String syncKey,
			Integer eventCollectionId, String... eventUids);
	
	void removeInvitationStatus(Integer eventCollectionId,  Integer emailCollectionId, Long email);
	
}
