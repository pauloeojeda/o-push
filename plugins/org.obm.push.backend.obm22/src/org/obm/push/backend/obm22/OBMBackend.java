package org.obm.push.backend.obm22;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.IHierarchyImporter;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.backend.obm22.calendar.CalendarBackend;
import org.obm.push.backend.obm22.contacts.ContactsBackend;
import org.obm.push.backend.obm22.impl.ListenerRegistration;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.provisioning.MSEASProvisioingWBXML;
import org.obm.push.provisioning.MSWAPProvisioningXML;
import org.obm.push.provisioning.Policy;
import org.obm.push.store.ISyncStorage;

public class OBMBackend implements IBackend {

	private IHierarchyImporter hImporter;
	private IContentsImporter cImporter;
	private IHierarchyExporter exporter;
	private IContentsExporter contentsExporter;
	private ISyncStorage store;

	private Set<ICollectionChangeListener> registeredListeners;

	private static final Log logger = LogFactory.getLog(OBMBackend.class);

	public OBMBackend(ISyncStorage store) {
		registeredListeners = Collections
				.synchronizedSet(new HashSet<ICollectionChangeListener>());

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
		if (bs.getProtocolVersion() <= 2.5) {
			return new MSWAPProvisioningXML();
		} else {
			return new MSEASProvisioingWBXML();
		}
	}

	public void onChangeFound(IContinuation continuation, BackendSession bs) {
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

	@Override
	public IListenerRegistration addChangeListener(ICollectionChangeListener ccl) {
		ListenerRegistration ret = new ListenerRegistration(ccl,
				registeredListeners);
		synchronized (registeredListeners) {
			registeredListeners.add(ccl);
		}
		logger.info("[" + ccl.getSession().getLoginAtDomain()
				+ "] change listener registered on backend");
		return ret;
	}

}
