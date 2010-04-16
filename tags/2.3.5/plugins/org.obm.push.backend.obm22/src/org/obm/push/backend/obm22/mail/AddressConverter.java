package org.obm.push.backend.obm22.mail;

import java.util.LinkedList;
import java.util.List;

import org.minig.imap.Address;
import org.obm.push.backend.MSAddress;

public class AddressConverter {
	
	private AddressConverter() {
	}
	
	public static MSAddress convertAddress(Address add){
		if(add == null){
			return null;
		}
		MSAddress msAdd = new MSAddress(add.getDisplayName(),add.getMail());

		return msAdd;
	}
	
	public static List<MSAddress> convertAddresses(Address[] adds){
		List<MSAddress> ret = new LinkedList<MSAddress>();
		for(Address add : adds){
			ret.add(convertAddress(add));
		}	
		return ret;
	}
	
	
}
