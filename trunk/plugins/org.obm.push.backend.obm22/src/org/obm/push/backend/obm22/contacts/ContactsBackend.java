package org.obm.push.backend.obm22.contacts;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;
import org.obm.push.backend.MSContact;
import org.obm.push.backend.obm22.impl.ObmSyncBackend;
import org.obm.push.backend.obm22.impl.UIDMapper;
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

	private BookClient getClient(BackendSession bs) {
		AddressBookLocator abl = new AddressBookLocator();
		if (obmSyncHost == null) {
			locateObmSync(bs);
		}
		BookClient bookCli = abl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		return bookCli;
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();

		ItemChange ic = new ItemChange();
		ic.setServerId("obm:\\\\" + bs.getLoginAtDomain() + "\\contacts");
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " contacts");
		ic.setItemType(FolderType.DEFAULT_CONTACTS_FOLDER);
		ret.add(ic);
		return ret;
	}

	public DataDelta getContentChanges(BackendSession bs) {
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		logger.info("getContentChanges(" + bs.getState().getLastSync() + ")");
		BookClient bc = getClient(bs);
		AccessToken token = bc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");

		try {
			ContactChanges changes = bc.getSync(token, BookType.contacts, bs
					.getState().getLastSync());
			for (Contact c : changes.getUpdated()) {
				ItemChange change = getContactChange(bs, c);
				addUpd.add(change);
			}
			bs.setUpdatedSyncDate(changes.getLastSync());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		bc.logout(token);
		return new DataDelta(addUpd, deletions);
	}

	private ItemChange getContactChange(BackendSession bs, Contact c) {
		ItemChange ic = new ItemChange();
		ic.setServerId(UIDMapper.UID_BOOK_PREFIX + c.getUid());
		MSContact cal = new ContactConverter().convert(c);

		String clientId = mapper.toDevice(bs.getDevId(), ic.getServerId());
		if (!ic.getServerId().equals(clientId)) {
			ic.setClientId(clientId);
		}
		ic.setData(cal);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, String collectionId,
			String serverId, String clientId, MSContact data) {
		logger.info("create in " + collectionId + " (contact: "
				+ data.getFirstName() + " " + data.getLastName() + ")");
		BookClient bc = getClient(bs);
		AccessToken token = bc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");

		String id = null;
		Contact oc = null;
		try {
			if (serverId != null) {
				id = serverId.replace(UIDMapper.UID_BOOK_PREFIX, "");
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
		id = UIDMapper.UID_BOOK_PREFIX + oc.getUid();
		mapper.addMapping(bs.getDevId(), clientId, id);

		return id;
	}
}
