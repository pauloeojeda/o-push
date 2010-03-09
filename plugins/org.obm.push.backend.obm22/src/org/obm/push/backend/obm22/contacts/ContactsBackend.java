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
import org.obm.push.store.ISyncStorage;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.locators.AddressBookLocator;

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

	private BookClient getBookClient(BackendSession bs) {
		AddressBookLocator abl = new AddressBookLocator();
		if (obmSyncHost == null) {
			locateObmSync(bs.getLoginAtDomain());
		}
		BookClient bookCli = abl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		return bookCli;
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		String col = "obm:\\\\" + bs.getLoginAtDomain() + "\\contacts";
		String serverId ;
		try {
			serverId = getServerIdFor(bs.getDevId(), col, null);
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

	public DataDelta getContentChanges(BackendSession bs, String collection) {
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		logger.info("getContentChanges(" + bs.getState().getLastSync() + ")");
		BookClient bc = getBookClient(bs);
		AccessToken token = bc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");

		try {
			ContactChanges changes = bc.getSync(token, BookType.contacts, bs
					.getState().getLastSync());
			
			long time = System.currentTimeMillis();
			for (Contact c : changes.getUpdated()) {
				ItemChange change = getContactChange(bs, collection, c);
				addUpd.add(change);
			}
			time = System.currentTimeMillis() - time;

			for (Integer del : changes.getRemoved()) {
				ItemChange change = getDeletion(bs, collection, "" + del);
				deletions.add(change);
			}

			time = System.currentTimeMillis() - time;

			bs.addUpdatedSyncDate(getCollectionIdFor(bs.getDevId(), collection), changes.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		bc.logout(token);
		return new DataDelta(addUpd, deletions);
	}

	private ItemChange getContactChange(BackendSession bs, String collection,
			Contact c) throws ActiveSyncException {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(bs.getDevId(), collection, ""
				+ c.getUid()));
		MSContact cal = new ContactConverter().convert(c);
		ic.setData(cal);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, String collectionId,
			String serverId, String clientId, MSContact data) throws ActiveSyncException {
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
				oc = bc.createContact(token, BookType.contacts,
						new ContactConverter().contact(data));
				id = oc.getUid().toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		bc.logout(token);

		return getServerIdFor(bs.getDevId(), collectionId, id);
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
}
