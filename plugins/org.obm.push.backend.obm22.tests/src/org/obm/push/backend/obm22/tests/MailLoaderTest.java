package org.obm.push.backend.obm22.tests;

import junit.framework.TestCase;

import org.minig.imap.StoreClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.MSEvent;
import org.obm.push.backend.obm22.mail.MailMessageLoader;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.CalendarLocator;

public class MailLoaderTest extends TestCase {

	public void testMailLoader() throws Exception {
		BackendSession bs = new BackendSession("thomas@zz.com", "aliacom",
				"devId", "devType", "command");
		StoreClient store = new StoreClient("obm23.buffy.kvm", 143,
				"thomas@zz.com", "aliacom");
		store.login();
		store.select("INBOX");
		CalendarLocator cl = new CalendarLocator();
		CalendarClient calCli = cl.locate("http://obm23.buffy.kvm:8080/obm-sync/services");
		MailMessageLoader mml = new MailMessageLoader(store, calCli);
		MSEmail mail = mml.fetch(25, 523, bs);
		assertNotNull(mail);
		MSEvent invit = mail.getInvitation();
		assertNotNull(invit);
		
		System.err.println("invit start: "+invit.getStartTime()+" dtstamp: "+invit.getDtStamp());
	}
	
	public void testMailLoader1() throws Exception {
		BackendSession bs = new BackendSession("adrien@test.tlse.lng", "aliacom",
				"devId", "devType", "command");
		StoreClient store = new StoreClient("obm", 143,
				"adrien@test.tlse.lng", "aliacom");
		store.login();
		store.select("INBOX");
		CalendarLocator cl = new CalendarLocator();
		CalendarClient calCli = cl.locate("http://obm:8080/obm-sync/services");
		MailMessageLoader mml = new MailMessageLoader(store, calCli);
		MSEmail mail = mml.fetch(315, 457, bs);
		assertNotNull(mail);
		MSEvent invit = mail.getInvitation();
//		assertNotNull(invit);
		
		System.err.println("invit start: "+invit.getStartTime()+" dtstamp: "+invit.getDtStamp());
	}

}
