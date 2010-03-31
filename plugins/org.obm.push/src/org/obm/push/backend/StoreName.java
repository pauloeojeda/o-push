package org.obm.push.backend;

import org.obm.push.exception.XMLValidationException;

public enum StoreName {
	Mailbox,DocumentLibrary,GAL;
	
	public static StoreName getValue(String value) throws XMLValidationException{
		if("Mailbox".equals(value)){
			return Mailbox;
		} else if("Document Library".equals(value)){
			return DocumentLibrary;
		} else if("GAL".equals(value)){
			return GAL;
		} 
		return null;
	}
}
