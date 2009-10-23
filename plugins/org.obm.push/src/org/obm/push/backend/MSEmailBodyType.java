package org.obm.push.backend;

/**
 * 
 * @author adrienp
 *
 */
public enum MSEmailBodyType {
	
	PlainText, HTML, RTF;

	public String asIntString() {
		switch (this) {
		case PlainText:
			return "1";
		case HTML:
			return "2";
		case RTF:
			return "3";
		default:
			return "0";
		}
	}

	public static final MSEmailBodyType getValueOf(String s) {
		if ("text/rtf".equals(s)) {
			return RTF;
		} else if ("text/html".equals(s)) {
			return HTML;
		} else {
			return PlainText;
		}
	}
}
