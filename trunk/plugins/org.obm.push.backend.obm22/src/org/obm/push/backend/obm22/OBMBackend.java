package org.obm.push.backend.obm22;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.IHierarchyImporter;
import org.obm.push.backend.SyncCollection;
import org.obm.push.backend.obm22.calendar.CalendarBackend;
import org.obm.push.backend.obm22.contacts.ContactsBackend;
import org.obm.push.backend.obm22.impl.PollingThread;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.provisioning.Policy;
import org.obm.push.store.ISyncStorage;

public class OBMBackend implements IBackend {

	private IHierarchyImporter hImporter;
	private IContentsImporter cImporter;
	private IHierarchyExporter exporter;
	private IContentsExporter contentsExporter;
	private ISyncStorage store;

	private static final Log logger = LogFactory.getLog(OBMBackend.class);

	public OBMBackend(ISyncStorage store) {
		MailBackend mailExporter = new MailBackend(store);
		CalendarBackend calendarExporter = new CalendarBackend(store);
		ContactsBackend contactsBackend = new ContactsBackend(store);
		this.store = store;

		hImporter = new HierarchyImporter();
		exporter = new HierarchyExporter(mailExporter, calendarExporter,
				contactsBackend);
		cImporter = new ContentsImporter(mailExporter, calendarExporter,
				contactsBackend);
		contentsExporter = new ContentsExporter(mailExporter, calendarExporter,
				contactsBackend);
	}

	@Override
	public IHierarchyImporter getHierarchyImporter(BackendSession bs) {
		return hImporter;
	}

	@Override
	public IHierarchyExporter getHierarchyExporter(BackendSession bs) {
		return exporter;
	}

	@Override
	public IContentsImporter getContentsImporter(String collectionId,
			BackendSession bs) {
		return cImporter;
	}

	@Override
	public String getWasteBasket() {
		return "Trash";
	}

	@Override
	public IContentsExporter getContentsExporter(BackendSession bs) {
		return contentsExporter;
	}

	@Override
	public Policy getDevicePolicy(BackendSession bs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<SyncCollection> pollForChanges(Continuation c,
			BackendSession bs, Set<SyncCollection> toMonitor, long msTimeout) {
		logger.info("starting polling thread");
		PollingThread pt = new PollingThread(bs, toMonitor, this, c, msTimeout);
		Thread t = new Thread(pt);
		t.start();

		synchronized (bs) {
			c.suspend(msTimeout);
		}
		logger.info("After suspend returned !!");
		return ((BackendSession) c.getObject()).getChangedFolders();
	}

	public void onChangeFound(Continuation continuation, BackendSession bs) {
		logger.info("onChangesFound");
		synchronized (bs) {
			continuation.setObject(bs);
			continuation.resume();
			logger.info("after resume !!");
		}
	}

	@Override
	public ISyncStorage getStore() {
		return store;
	}

	@Override
	public void sendMail(BackendSession bs, byte[] mailContent) {
		logger.warn("not implemented: should send email");
	}

}
