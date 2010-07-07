package org.obm.push.backend.obm22;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.IHierarchyImporter;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.backend.obm22.calendar.CalendarBackend;
import org.obm.push.backend.obm22.calendar.CalendarMonitoringThread;
import org.obm.push.backend.obm22.contacts.ContactsBackend;
import org.obm.push.backend.obm22.contacts.ContactsMonitoringThread;
import org.obm.push.backend.obm22.impl.ListenerRegistration;
import org.obm.push.backend.obm22.mail.EmailManager;
import org.obm.push.backend.obm22.mail.EmailMonitoringThread;
import org.obm.push.backend.obm22.mail.MailBackend;
import org.obm.push.exception.ActiveSyncException;
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
	private CalendarMonitoringThread calendarPushMonitor;
	private ContactsMonitoringThread contactsPushMonitor;
	private Map<Integer, EmailMonitoringThread> emailPushMonitors; 

	private static final Log logger = LogFactory.getLog(OBMBackend.class);

	public OBMBackend(ISyncStorage store) {
		registeredListeners = Collections
				.synchronizedSet(new HashSet<ICollectionChangeListener>());
		emailPushMonitors = Collections.synchronizedMap(new HashMap<Integer, EmailMonitoringThread>());
		FolderBackend folderExporter = new FolderBackend(store);
		MailBackend mailBackend = new MailBackend(store);
		CalendarBackend calendarBackend = new CalendarBackend(store);
		ContactsBackend contactsBackend = new ContactsBackend(store);
//		TasksBackend tasksBackend = new TasksBackend(store);
		this.store = store;

		hImporter = new HierarchyImporter();
		exporter = new HierarchyExporter(folderExporter, mailBackend,
				calendarBackend, contactsBackend);
		cImporter = new ContentsImporter(mailBackend, calendarBackend,
				contactsBackend);
		contentsExporter = new ContentsExporter(mailBackend, calendarBackend,
				contactsBackend);

		startOBMMonitoringThreads(calendarBackend);
	}

	private void startOBMMonitoringThreads(CalendarBackend cb) {
		calendarPushMonitor = new CalendarMonitoringThread(cb, 5000,
				registeredListeners);
		Thread calThread = new Thread(calendarPushMonitor);
		calThread.setDaemon(true);
		calThread.start();

		contactsPushMonitor = new ContactsMonitoringThread(
				cb, 5000, registeredListeners);
		Thread contactThread = new Thread(contactsPushMonitor);
		contactThread.setDaemon(true);
		contactThread.start();
	}
	
	public void startEmailMonitoring(BackendSession bs, Integer collectionId) throws ActiveSyncException{
		EmailMonitoringThread emt = emailPushMonitors.get(collectionId);
		if(emt != null){
			emt.stopIdle();
		} else {
			MailBackend mailBackend = new MailBackend(store);
			emt = new EmailMonitoringThread(mailBackend, registeredListeners,bs,collectionId);
		}
		try {
			emt.startIdle();
		} catch (Exception e) {
			logger.error("Error while starting idle on collection["+collectionId+"]", e);
			emt.stopIdle();
		}
		emailPushMonitors.put(collectionId, emt);
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
	public IContentsImporter getContentsImporter(Integer collectionId,
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
			return new MSEASProvisioingWBXML(bs.getProtocolVersion());
		}
	}


	@Override
	public ISyncStorage getStore() {
		return store;
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
	
	@Override
	public void resetForFullSync(BackendSession bs) {
		logger.info("resetForFullSync devId: " + bs.getDevId());
//		try {
//			Set<Integer> colIds = getStore().getAllCollectionId(bs.getDevId());
//			EmailManager.getInstance().resetForFullSync(colIds);
//			getStore().resetForFullSync(bs.getDevId());
//			bs.clearAll();
//		} catch (RuntimeException re) {
//			logger.error(re.getMessage(), re);
//			throw re;
//		}
	}

	@Override
	public void resetCollection(BackendSession bs, Integer collectionId) {
		logger.info("reset Collection "+collectionId+" For Full Sync devId: " + bs.getDevId());
		try {
			Set<Integer> colIds = new HashSet<Integer>();
			colIds.add(collectionId);
			EmailManager.getInstance().resetForFullSync(colIds);
			getStore().resetCollection(bs.getDevId(),collectionId);
			bs.clear(collectionId);
		} catch (RuntimeException re) {
			logger.error(re.getMessage(), re);
			throw re;
		}
	}

	@Override
	public boolean validatePassword(String userID, String password) {
		return contentsExporter.validatePassword(userID, password);
	}
}
