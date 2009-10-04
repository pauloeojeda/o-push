package org.obm.push.backend.obm22.calendar;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.SyncCollection;
import org.obm.push.backend.obm22.impl.ChangedCollections;
import org.obm.push.backend.obm22.impl.MonitoringThread;

public class CalendarMonitoringThread extends MonitoringThread {

	public CalendarMonitoringThread(long freqMs, Set<ICollectionChangeListener> ccls) {
		super(freqMs, ccls);
	}

	@Override
	protected ChangedCollections getChangedCollections(Date lastSync) {
		
		Date dbDate = lastSync;
		Set<SyncCollection> changed = new HashSet<SyncCollection>();
		
		// TODO Auto-generated method stub
		logger.info("changed collections: "+changed.size()+" dbDate: "+dbDate);
		
		return new ChangedCollections(dbDate, changed);
	}

}
