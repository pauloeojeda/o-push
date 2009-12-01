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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set; //import java.util.concurrent.Semaphore;
//import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.smtp.SMTPProtocol;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.StoreClient;
import org.obm.locator.client.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.MSEmail;
import org.obm.sync.client.calendar.CalendarClient;

/**
 * 
 * @author adrienp
 * 
 */
public class EmailManager {

	protected String imapHost;
	protected String smtpHost;

	private static EmailManager instance;

	private Log logger;
	private Map<Integer, EmailCacheStorage> uidCache;

	private EmailManager() {
		this.logger = LogFactory.getLog(getClass());
		this.uidCache = new HashMap<Integer, EmailCacheStorage>();
	}

	public static EmailManager getInstance() {
		if (instance == null) {
			instance = new EmailManager();
		}
		return instance;
	}

	private void locateSmtp(BackendSession bs) {
		smtpHost = new LocatorClient().locateHost("mail/smtp_out", bs
				.getLoginAtDomain());
		logger.info("Using " + smtpHost + " as smtp host.");
	}

	private void locateImap(BackendSession bs) {
		imapHost = new LocatorClient().locateHost("mail/imap", bs
				.getLoginAtDomain());
		logger.info("Using " + imapHost + " as imap host.");
	}

	private StoreClient getImapClient(BackendSession bs) {
		if (imapHost == null) {
			locateImap(bs);
		}
		StoreClient imapCli = new StoreClient(imapHost, 143, bs
				.getLoginAtDomain(), bs.getPassword());
		return imapCli;
	}

	private SMTPProtocol getSmtpClient(BackendSession bs) {
		if (smtpHost == null) {
			locateSmtp(bs);
		}
		SMTPProtocol proto = new SMTPProtocol(smtpHost);
		return proto;
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
		MailChanges sync = uc.getSync(getImapClient(bs), devId, bs,
				collectionId, parseMailBoxName(bs, collectionName));
		return sync;
	}

	public List<MSEmail> fetchMails(BackendSession bs,
			CalendarClient calendarClient, Integer collectionId, String collectionName, Set<Long> uids)
			throws IOException, IMAPException {
		List<MSEmail> mails = new LinkedList<MSEmail>();
		StoreClient store = getImapClient(bs);
		login(store);
		store.select(parseMailBoxName(bs, collectionName));
		for (Long uid : uids) {
			MailMessageLoader mailLoader =  new MailMessageLoader();
			mails.add(mailLoader.fetch(collectionId, uid, store, bs, calendarClient));
		}
		store.logout();
		return mails;
	}
	
	public List<InputStream> fetchMIMEMails(BackendSession bs,
			CalendarClient calendarClient, String collectionName, Set<Long> uids)
			throws IOException, IMAPException {
		List<InputStream> mails = new LinkedList<InputStream>();
		StoreClient store = getImapClient(bs);
		login(store);
		store.select(parseMailBoxName(bs, collectionName));
		for (Long uid : uids) {
			mails.add(store.uidFetchMessage(uid));
		}
		store.logout();
		return mails;
	}

	public ListResult listAllFolder(BackendSession bs) throws IMAPException {
		StoreClient store = getImapClient(bs);
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
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs, collectionName);
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

	public String parseMailBoxName(BackendSession bs, String collectionName)
			throws IMAPException {
		// parse obm:\\adrien@test.tlse.lng\email\INBOX\Sent
		int slash = collectionName.lastIndexOf("email\\");
		String boxName = collectionName.substring(slash + "email\\".length());
		ListResult lr = listAllFolder(bs);
		for (ListInfo i : lr) {
			if (i.getName().toLowerCase().contains(boxName.toLowerCase())) {
				return i.getName();
			}
		}
		throw new IMAPException("Cannot find IMAP folder for collection["
				+ collectionName + "]");
	}

	public void resetForFullSync(Set<Integer> listCollectionId) {
		for (Integer colId : listCollectionId) {
			uidCache.remove(colId);
		}
		logger.info("resetForFullSync");
	}

	public void delete(BackendSession bs, Integer devId, String collectionName,
			Integer collectionId, Long uid) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id : " + uid);
			long[] uids = { uid };
			store.uidStore(uids, fl, true);
			store.expunge();
			deleteMessageInCache(devId, collectionId, uid);
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
	}

	public Long moveItem(BackendSession bs, Integer devId, String srcFolder,
			Integer srcFolderId, String dstFolder, Integer dstFolderId, Long uid)
			throws IMAPException {
		StoreClient store = getImapClient(bs);
		long[] newUid = null;
		try {
			login(store);
			String srcMailBox = parseMailBoxName(bs, srcFolder);
			String dstMailBox = parseMailBoxName(bs, dstFolder);
			store.select(srcMailBox);
			long[] uids = { uid };
			newUid = store.uidCopy(uids, dstMailBox);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id : " + uid);
			store.uidStore(uids, fl, true);
			store.expunge();
			deleteMessageInCache(devId, srcFolderId, uid);
			addMessageInCache(devId, dstFolderId, uid);
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
		if (newUid == null || newUid.length == 0) {
			return null;
		}
		return newUid[0];
	}

	private void login(StoreClient store) throws IMAPException {
		if (!store.login()) {
			throw new IMAPException("Cannot log into imap server");
		}
	}

	public void setAnsweredFlag(BackendSession bs, String collectionName,
			Long uid) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.ANSWERED);
			long[] uids = { uid };
			store.uidStore(uids, fl, true);
			logger
					.info("flag  change: "
							+ ("+ ANSWERED" + " on mail " + uid + " in " + mailBoxName));
		} finally {
			store.logout();
		}
	}

	public void sendEmail(BackendSession bs, String from, Set<Address> setTo,
			String mimeMail, Boolean saveInSent) {
		try {
			logger.info("Send mail to " + from + ":\n" + mimeMail);
			SMTPProtocol smtp = getSmtpClient(bs);
			smtp.openPort();
			smtp.ehlo(InetAddress.getLocalHost());
			Address addrFrom = new Address(from);
			smtp.mail(addrFrom);

			for (Address to : setTo) {
				smtp.rcpt(to);
			}

			InputStream data = new ByteArrayInputStream(mimeMail.getBytes());
			smtp.data(data);
			smtp.quit();

			if (saveInSent) {
				storeInSent(bs, mimeMail.getBytes());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public InputStream findAttachment(BackendSession bs, String collectionName, Long mailUid,  String mimePartAddress) throws IMAPException{
		
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs, collectionName);
			store.select(mailBoxName);
			return store.uidFetchPart(mailUid, mimePartAddress);
		} finally {
			store.logout();
		}
	}

	private void storeInSent(BackendSession bs, byte[] mailContent)
			throws IMAPException {
		String sentFolderName = null;
		ListResult lr = listAllFolder(bs);
		for (ListInfo i : lr) {
			if (i.getName().toLowerCase().endsWith("sent")) {
				sentFolderName = i.getName();
			}
		}

		if (sentFolderName != null) {
			StoreClient store = getImapClient(bs);
			try {
				login(store);
				InputStream in = new ByteArrayInputStream(mailContent);
				FlagsList fl = new FlagsList();
				fl.add(Flag.SEEN);
				store.append(sentFolderName, in, fl);
				store.expunge();
			} finally {
				try {
					store.logout();
				} catch (IMAPException e) {
				}
			}
		}
	}

	private void deleteMessageInCache(Integer devId, Integer collectionId,
			Long mailUid) {
		EmailCacheStorage uc = cache(collectionId, false);
		uc.deleteMessage(devId, collectionId, mailUid);
	}

	private void addMessageInCache(Integer devId, Integer collectionId,
			Long mailUid) {
		EmailCacheStorage uc = cache(collectionId, false);
		uc.addMessage(devId, collectionId, mailUid);
	}
}
