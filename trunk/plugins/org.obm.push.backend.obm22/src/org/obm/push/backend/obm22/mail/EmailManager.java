/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.backend.obm22.mail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set; //import java.util.concurrent.Semaphore;
//import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.StoreClient;
import org.obm.locator.client.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.MSEmail;

/**
 * 
 * @author adrienp
 * 
 */
public class EmailManager {

	private MailMessageLoader mailLoader;
	protected String imapHost;

	private static EmailManager instance;

	private Log logger;
	private Map<Integer, EmailCacheStorage> uidCache;

	private EmailManager() {
		this.logger = LogFactory.getLog(getClass());
		mailLoader = new MailMessageLoader();
		this.uidCache = new HashMap<Integer, EmailCacheStorage>();
	}

	public static EmailManager getInstance() {
		if (instance == null) {
			instance = new EmailManager();
		}
		return instance;
	}

	private void locateImap(BackendSession bs) {
		imapHost = new LocatorClient().locateHost("mail/imap", bs
				.getLoginAtDomain());
		logger.info("Using " + imapHost + " as imap host.");
	}

	private StoreClient getClient(BackendSession bs) {
		if (imapHost == null) {
			locateImap(bs);
		}
		StoreClient imapCli = new StoreClient(imapHost, 143, bs
				.getLoginAtDomain(), bs.getPassword());
		return imapCli;
	}

	private EmailCacheStorage cache(Integer collectionId, Boolean reset) {
		EmailCacheStorage ret = uidCache.get(collectionId);
		if (ret == null) {
			ret = new EmailCacheStorage();
			uidCache.put(collectionId, ret);
		}
		return ret;
	}

	public MailChanges getSync(BackendSession bs, Integer devId,
			Integer collectionId, String collectionName)
			throws InterruptedException, SQLException, IMAPException {

		EmailCacheStorage uc = cache(collectionId, false);
		MailChanges sync = uc.getSync(getClient(bs), devId, bs, collectionId,
				parseMailBoxName(bs,collectionName));

		return sync;
	}

	public List<MSEmail> fetchMails(BackendSession bs, String collectionName,
			Set<Long> uids) throws IOException, IMAPException {
		List<MSEmail> mails = new LinkedList<MSEmail>();
		StoreClient store = getClient(bs);
		login(store);
		store.select(parseMailBoxName(bs,collectionName));
		for (Long uid : uids) {
			mails.add(mailLoader.fetch(uid, store));
		}
		store.logout();
		return mails;
	}

	public ListResult listAllFolder(BackendSession bs) throws IMAPException {
		StoreClient store = getClient(bs);
		ListResult ret = new ListResult(0);
		try {
			login(store);
			ret = store.listAll("", "");
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
		return ret;
	}

	public void updateReadFlag(BackendSession bs, String collectionName,
			Long uid, boolean read) throws IMAPException {
		StoreClient store = getClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs,collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.SEEN);
			long[] uids = { uid };
			store.uidStore(uids, fl, read);
			logger.info("flag  change: " + (read ? "+" : "-") + " SEEN"
					+ " on mail " + uid + " in " + mailBoxName);
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
	}

	private String parseMailBoxName(BackendSession bs, String collectionName) throws IMAPException {
		// parse obm:\\adrien@test.tlse.lng\email\INBOX\Sent
		int slash = collectionName.lastIndexOf("email\\");
		String boxName = collectionName.substring(slash + "email\\".length());
		ListResult lr = listAllFolder(bs);
		for (ListInfo i : lr) {
			if (i.getName().toLowerCase().contains(boxName.toLowerCase())) {
				return i.getName();
			}
		}
		throw new IMAPException("Cannot find IMAP folder for collection["+collectionName+"]");
	}

	public void resetForFullSync(Set<Integer> listCollectionId) {
		for (Integer colId : listCollectionId) {
			uidCache.remove(colId);
		}
		logger.info("resetForFullSync");
	}

	public void delete(BackendSession bs, String collectionName, Long uid) throws IMAPException {
		StoreClient store = getClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs,collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id : " + uid);
			long[] uids = { uid };
			store.uidStore(uids, fl, true);
			store.expunge();
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
	}
	
	private void login(StoreClient store) throws IMAPException{
		if(!store.login()){
			throw new IMAPException("Cannot log into imap server");
		}
	}

}
