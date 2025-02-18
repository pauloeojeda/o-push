package org.obm.push.backend.obm22.contacts;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSContact;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.state.SyncState;
import org.obm.push.store.ISyncStorage;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.items.ContactChanges;

/**
 * OBM contacts backend implementation
 * 
 * @author tom
 * 
 */
public class ContactsBackend extends ObmSyncBackend {

	public ContactsBackend(ISyncStorage storage) {
		super(storage);
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		String col = "obm:\\\\" + bs.getLoginAtDomain() + "\\contacts";
		String serverId;
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevId(), col);
			serverId = getServerIdFor(collectionId, null);
		} catch (ActiveSyncException e) {
			serverId = createCollectionMapping(bs.getDevId(), col);
			ic.setIsNew(true);
		}

		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " contacts");
		ic.setItemType(FolderType.DEFAULT_CONTACTS_FOLDER);
		ret.add(ic);
		return ret;
	}

	public DataDelta getContentChanges(BackendSession bs, SyncState state,
			Integer collectionId) {
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		logger.info("getContentChanges(" + state.getLastSync() + ")");
		BookClient bc = getBookClient(bs);
		AccessToken token = bc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");

		try {
			ContactChanges changes = bc.getSync(token, BookType.contacts, state
					.getLastSync());

			long time = System.currentTimeMillis();
			for (Contact c : changes.getUpdated()) {
				ItemChange change = getContactChange(collectionId, c);
				addUpd.add(change);
			}
			time = System.currentTimeMillis() - time;

			for (Integer del : changes.getRemoved()) {
				ItemChange change = getDeletion(collectionId, "" + del);
				deletions.add(change);
			}

			time = System.currentTimeMillis() - time;

			bs.addUpdatedSyncDate(
					collectionId, changes
							.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		bc.logout(token);
		return new DataDelta(addUpd, deletions);
	}

	private ItemChange getContactChange( Integer collectionId,
			Contact c) throws ActiveSyncException {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(collectionId, ""
				+ c.getUid()));
		MSContact cal = new ContactConverter().convert(c);
		ic.setData(cal);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, String clientId, MSContact data)
			throws ActiveSyncException {
		logger.info("create in " + collectionId + " (contact: "
				+ data.getFirstName() + " " + data.getLastName() + ")");
		BookClient bc = getBookClient(bs);
		AccessToken token = bc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");

		String id = null;
		Contact oc = null;
		try {
			if (serverId != null) {
				int idx = serverId.lastIndexOf(":");
				id = serverId.substring(idx + 1);
				oc = new ContactConverter().contact(data);
				oc.setUid(Integer.parseInt(id));
				oc = bc.modifyContact(token, BookType.contacts, oc);
			} else {
				oc = bc.createContactWithoutDuplicate(token, BookType.contacts,
						new ContactConverter().contact(data));
				id = oc.getUid().toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		bc.logout(token);

		return getServerIdFor(collectionId, id);
	}

	public void delete(BackendSession bs, String serverId) {
		logger.info("delete serverId " + serverId);
		if (serverId != null) {
			int idx = serverId.indexOf(":");
			if (idx > 0) {
				String id = serverId.substring(idx + 1);
				BookClient bc = getBookClient(bs);
				AccessToken token = bc.login(bs.getLoginAtDomain(), bs
						.getPassword(), "o-push");
				try {
					bc.removeContact(token, BookType.contacts, id);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				bc.logout(token);
			}
		}
	}

	public List<ItemChange> fetchItems(BackendSession bs,
			List<String> fetchServerIds) throws ObjectNotFoundException {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		try {
			for (String serverId : fetchServerIds) {
				Integer id = getItemIdFor(serverId);
				if (id != null) {
					BookClient bc = getBookClient(bs);
					AccessToken token = bc.login(bs.getLoginAtDomain(), bs
							.getPassword(), "o-push");

					Contact c = bc.getContactFromId(token, BookType.contacts,
							id.toString());
					ItemChange ic = new ItemChange();
					ic.setServerId(serverId);
					MSContact cal = new ContactConverter().convert(c);
					ic.setData(cal);
					ret.add(ic);
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ObjectNotFoundException();
		}
		return ret;
	}

}
