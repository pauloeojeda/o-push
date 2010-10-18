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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.smtp.SMTPProtocol;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.locator.client.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.MSEmail;
import org.obm.push.state.SyncState;
import org.obm.sync.client.calendar.CalendarClient;

import fr.aliasource.utils.FileUtils;
import fr.aliasource.utils.IniFile;

/**
 * 
 * @author adrienp
 * 
 */
public class EmailManager {

	private static final String BACKEND_CONF_FILE = "/etc/opush/mail_conf.ini";
	private static final String BACKEND_IMAP_LOGIN_WITH_DOMAIN = "imap.loginWithDomain";
	private static final String BACKEND_IMAP_ACTIVATE_TLS = "imap.activateTLS";
	

	private Boolean loginWithDomain;
	private Boolean activateTLS;
	protected String imapHost;
	protected String smtpHost;

	private static EmailManager instance;

	static {
		instance = new EmailManager();
	}

	private Log logger;
	private Map<Integer, IEmailSync> uidCache;

	private EmailManager() {
		this.logger = LogFactory.getLog(getClass());
		this.uidCache = new HashMap<Integer, IEmailSync>();
		IniFile ini = new IniFile(BACKEND_CONF_FILE) {
			@Override
			public String getCategory() {
				return null;
			}
		};
		loginWithDomain = !"false".equals(ini.getData().get(
				BACKEND_IMAP_LOGIN_WITH_DOMAIN));
		activateTLS = !"false".equals(ini.getData().get(
				BACKEND_IMAP_ACTIVATE_TLS));
	}

	public static EmailManager getInstance() {
		return instance;
	}

	private void locateSmtp(BackendSession bs) {
		smtpHost = new LocatorClient().locateHost("mail/smtp_out",
				bs.getLoginAtDomain());
		logger.info("Using " + smtpHost + " as smtp host.");
	}

	private void locateImap(BackendSession bs) {
		imapHost = new LocatorClient().locateHost("mail/imap_frontend",
				bs.getLoginAtDomain());
		logger.info("Using " + imapHost + " as imap host.");
	}

	private StoreClient getImapClient(BackendSession bs) {
		if (imapHost == null) {
			locateImap(bs);
		}
		String login = bs.getLoginAtDomain();
		if (!loginWithDomain) {
			int at = login.indexOf("@");
			if (at > 0) {
				login = login.substring(0, at);
			}
		}
		logger.info("creating storeClient with login: " + login
				+ " (loginWithDomain: " + loginWithDomain + ", activateTLS:"+activateTLS+")");
		StoreClient imapCli = new StoreClient(imapHost, 143, login,
				bs.getPassword());

		return imapCli;
	}

	private SMTPProtocol getSmtpClient(BackendSession bs) {
		if (smtpHost == null) {
			locateSmtp(bs);
		}
		SMTPProtocol proto = new SMTPProtocol(smtpHost);
		return proto;
	}

	private IEmailSync cache(Integer collectionId, Boolean reset) {
		IEmailSync ret = uidCache.get(collectionId);
		if (ret == null) {
			ret = new EmailCacheStorage();
			uidCache.put(collectionId, ret);
		}
		return ret;
	}

	public MailChanges getSync(BackendSession bs, SyncState state,
			Integer devId, Integer collectionId, String collectionName)
			throws InterruptedException, SQLException, IMAPException {
		IEmailSync uc = cache(collectionId, false);
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBox = parseMailBoxName(store, bs, collectionName);
			store.select(mailBox);
			MailChanges sync = uc
					.getSync(store, devId, bs, state, collectionId);
			return sync;
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
	}

	public List<MSEmail> fetchMails(BackendSession bs,
			CalendarClient calendarClient, Integer collectionId,
			String collectionName, Set<Long> uids) throws IOException,
			IMAPException {
		List<MSEmail> mails = new LinkedList<MSEmail>();
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(store, bs, collectionName));
			MailMessageLoader mailLoader = new MailMessageLoader(store,
					calendarClient);
			for (Long uid : uids) {
				MSEmail email = mailLoader.fetch(collectionId, uid, bs);
				if (email != null) {
					mails.add(email);
				}
			}
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
		return mails;
	}

	public List<InputStream> fetchMIMEMails(BackendSession bs,
			CalendarClient calendarClient, String collectionName, Set<Long> uids)
			throws IOException, IMAPException {
		List<InputStream> mails = new LinkedList<InputStream>();
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(store, bs, collectionName));
			for (Long uid : uids) {
				mails.add(store.uidFetchMessage(uid));
			}
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
		return mails;
	}

	private ListResult listAllFolder(BackendSession bs) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			return listAllFolder(store, bs);
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
	}

	private ListResult listAllFolder(StoreClient store, BackendSession bs)
			throws IMAPException {
		ListResult ret = new ListResult(0);
		ret = store.listAll("", "");
		return ret;
	}

	public void updateReadFlag(BackendSession bs, String collectionName,
			Long uid, boolean read) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, bs, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.SEEN);
			store.uidStore(Arrays.asList(uid), fl, read);
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
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			return parseMailBoxName(store, bs, collectionName);
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
	}

	private String parseMailBoxName(StoreClient store, BackendSession bs,
			String collectionName) throws IMAPException {
		// parse obm:\\adrien@test.tlse.lng\email\INBOX\Sent
		int slash = collectionName.lastIndexOf("email\\");
		String boxName = collectionName.substring(slash + "email\\".length());
		ListResult lr = listAllFolder(store, bs);
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
	}

	public void delete(BackendSession bs, Integer devId, String collectionPath,
			Integer collectionId, Long uid) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, bs, collectionPath);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id : " + uid);
			store.uidStore(Arrays.asList(uid), fl, true);
			store.expunge();
			deleteMessageInCache(devId, collectionId, Arrays.asList(uid));
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
		Collection<Long> newUid = null;
		try {
			login(store);
			String srcMailBox = parseMailBoxName(store, bs, srcFolder);
			String dstMailBox = parseMailBoxName(store, bs, dstFolder);
			store.select(srcMailBox);
			List<Long> uids = Arrays.asList(uid);
			newUid = store.uidCopy(uids, dstMailBox);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id : " + uid);
			store.uidStore(uids, fl, true);
			store.expunge();
			deleteMessageInCache(devId, srcFolderId, Arrays.asList(uid));
			addMessageInCache(devId, dstFolderId, uid);
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
		if (newUid == null || newUid.isEmpty()) {
			return null;
		}
		return newUid.iterator().next();
	}

	private void login(StoreClient store) throws IMAPException {
		if (!store.login(activateTLS)) {
			throw new IMAPException("Cannot log into imap server");
		}
	}

	public void setAnsweredFlag(BackendSession bs, String collectionName,
			Long uid) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, bs, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.ANSWERED);
			store.uidStore(Arrays.asList(uid), fl, true);
			logger.info("flag  change: "
					+ ("+ ANSWERED" + " on mail " + uid + " in " + mailBoxName));
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
	}

	public void sendEmail(BackendSession bs, String from, Set<Address> setTo,
			Set<Address> setCc, Set<Address> setCci, InputStream mimeMail,
			Boolean saveInSent) {
		try {
			logger.info("Send mail to " + setTo);
			if (!mimeMail.markSupported()) {
				ByteArrayOutputStream outPut = new ByteArrayOutputStream();
				FileUtils.transfer(mimeMail, outPut, true);
				mimeMail = new ByteArrayInputStream(outPut.toByteArray());
			}
			SMTPProtocol smtp = getSmtpClient(bs);
			smtp.openPort();
			smtp.ehlo(InetAddress.getLocalHost());
			Address addrFrom = new Address(from);
			smtp.mail(addrFrom);
			Address[] recipients = getAllRistrettoRecipients(setTo, setCc,
					setCci);
			for (Address to : recipients) {
				Address cleaned = new Address(to.getMailAddress());
				smtp.rcpt(cleaned);
			}
			smtp.data(mimeMail);
			smtp.quit();
			if (saveInSent) {
				mimeMail.reset();
				storeInSent(bs, mimeMail);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Address[] getAllRistrettoRecipients(Set<Address> to,
			Set<Address> cc, Set<Address> bcc) {
		org.columba.ristretto.message.Address addrs[] = new org.columba.ristretto.message.Address[to
				.size() + cc.size() + bcc.size()];
		int i = 0;
		for (Address addr : to) {
			addrs[i++] = addr;
		}
		for (Address addr : cc) {
			addrs[i++] = addr;
		}
		for (Address addr : bcc) {
			addrs[i++] = addr;
		}
		return addrs;
	}

	public InputStream findAttachment(BackendSession bs, String collectionName,
			Long mailUid, String mimePartAddress) throws IMAPException {

		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, bs, collectionName);
			store.select(mailBoxName);
			return store.uidFetchPart(mailUid, mimePartAddress);
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}
	}

	public void purgeFolder(BackendSession bs, Integer devId,
			String collectionPath, Integer collectionId) throws IMAPException {
		long time = System.currentTimeMillis();
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, bs, collectionPath);
			store.select(mailBoxName);
			logger.info("Mailbox folder[" + collectionPath
					+ "] will be purged...");
			Collection<Long> uids = store.uidSearch(new SearchQuery());
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			store.uidStore(uids, fl, true);
			store.expunge();
			deleteMessageInCache(devId, collectionId, uids);
			time = System.currentTimeMillis() - time;
			logger.info("Mailbox folder[" + collectionPath + "] was purged in "
					+ time + " millisec. " + uids.size()
					+ " messages have been deleted");
		} finally {
			try {
				store.logout();
			} catch (IMAPException e) {
			}
		}

	}

	private void storeInSent(BackendSession bs, InputStream mailContent)
			throws IMAPException, IOException {
		logger.info("Store mail in folder[Sent]");
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
				FlagsList fl = new FlagsList();
				fl.add(Flag.SEEN);
				store.append(sentFolderName, mailContent, fl);
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
			Collection<Long> mailUids) {
		IEmailSync uc = cache(collectionId, false);
		for (Long uid : mailUids) {
			uc.deleteMessage(devId, collectionId, uid);
		}
	}

	private void addMessageInCache(Integer devId, Integer collectionId,
			Long mailUid) {
		IEmailSync uc = cache(collectionId, false);
		uc.addMessage(devId, collectionId, mailUid);
	}

	public Boolean getLoginWithDomain() {
		return loginWithDomain;
	}
	
	public Boolean getActivateTLS() {
		return activateTLS;
	}
}
