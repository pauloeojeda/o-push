package org.obm.push.backend.obm22.calendar;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.FolderType;
import org.obm.push.backend.ItemChange;

public class CalendarExporter {

	public List<ItemChange> getHierarchyChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		
		ItemChange ic = new ItemChange();
		ic.setServerId("obm://"+bs.getLoginAtDomain()+"/calendar/"+bs.getLoginAtDomain());
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain());
		ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
		ret.add(ic);

		ic = new ItemChange();
		ic.setServerId("obm://thomas@zz.com/calendar/sylvaing@zz.com");
		ic.setParentId("0");
		ic.setDisplayName("sylvaing@zz.com");
		ic.setItemType(FolderType.USER_CREATED_CALENDAR_FOLDER);
		ret.add(ic);

		return ret;
	}

	public List<ItemChange> getContentChanges(BackendSession bs) {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		
		
		return ret;
	}
	
}
