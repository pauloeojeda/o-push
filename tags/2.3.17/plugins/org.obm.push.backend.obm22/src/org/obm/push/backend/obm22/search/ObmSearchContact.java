package org.obm.push.backend.obm22.search;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.locator.client.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.obm22.contacts.ContactConverter;
import org.obm.push.search.ISearchSource;
import org.obm.push.search.SearchResult;
import org.obm.push.search.StoreName;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.locators.AddressBookLocator;

/**
 * 
 * @author adrienp
 * 
 */
public class ObmSearchContact implements ISearchSource {

	protected Log logger = LogFactory.getLog(getClass());
	protected String obmSyncHost;
	
	public ObmSearchContact() {
	}

	public StoreName getStoreName() {
		return StoreName.GAL;
	}

	public List<SearchResult> search(BackendSession bs, String query,
			Integer limit) {
		BookClient bc = getBookClient(bs);
		AccessToken token = bc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		List<SearchResult> ret = new LinkedList<SearchResult>();
		ContactConverter cc = new ContactConverter();
		try {
			List<Contact> contacts = bc.searchContact(token, query, limit);
			for (Contact contact : contacts) {
				ret.add(cc.convertToSearchResult(contact));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ret;
	}
	
	protected BookClient getBookClient(BackendSession bs) {
		AddressBookLocator abl = new AddressBookLocator();
		if (obmSyncHost == null) {
			locateObmSync(bs.getLoginAtDomain());
		}
		BookClient bookCli = abl.locate("http://" + obmSyncHost
				+ ":8080/obm-sync/services");
		return bookCli;
	}
	
	protected void locateObmSync(String loginAtDomain) {
		obmSyncHost = new LocatorClient().locateHost("sync/obm_sync", loginAtDomain);
		logger.info("Using " + obmSyncHost + " as obm_sync host.");
	}
}
