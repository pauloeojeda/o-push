package org.obm.push.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.impl.SyncHandler;
import org.obm.push.utils.Base64;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class Decoder {
	protected static final Log logger = LogFactory.getLog(SyncHandler.class);
	
	public String parseDOMString (Element elt, String default_value) {
		if (elt != null) {
			return elt.getTextContent();
		}
		return default_value;
	}
	
	public String parseDOMString (Element elt) {
		return parseDOMString(elt, null);
	}
	
	public SimpleDateFormat parseDOMDate (Element elt, SimpleDateFormat default_value) {
		if (elt != null) {
			return parseDate(elt.getTextContent());
		}
		return default_value;
	}
	
	public SimpleDateFormat parseDOMDate (Element elt) {
		return parseDOMDate(elt, null);
	}
	
	public Byte parseDOMByte (Element elt, Byte default_value) {
		if (elt != null) {
			return parseByte(elt.getTextContent());
		}
		return default_value;
	}
	
	public Byte parseDOMByte (Element elt) {
		return parseDOMByte(elt, null);
	}
	
	public Integer parseDOMInt (Element elt, Integer default_value) {
		if (elt != null) {
			return parseInt(elt.getTextContent());
		}
		return default_value;
	}
	
	public Integer parseDOMInt (Element elt) {
		return parseDOMInt(elt, null);
	}
	
	public SimpleDateFormat parseDate (String str) {
		SimpleDateFormat date;
		
		// Doc : [MS-ASDTYPE] 2.6 Date/Time
		try {
			if (str.matches("^....-..-..T..:..:..\\....Z$")) {
				date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'");
				date.setTimeZone(TimeZone.getTimeZone("GMT"));
				date.parse(str);
				logger.info("parse date: "+ date.getCalendar().getTime().toString());
				
				return date;
				
			} else if (str.matches("^........T......Z$")) {
				date = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
				date.setTimeZone(TimeZone.getTimeZone("GMT"));
				date.parse(str);
				logger.info("parse date: "+ date.getCalendar().getTime().toString());
				
				return date;
				
			} else {
				logger.error("Not parsable date " + str);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public TimeZone parseDOMTimeZone (Element node) {
		return parseDOMTimeZone(node, null);
	}
	
	public TimeZone parseDOMTimeZone (Element node, TimeZone default_value) {
		if (node != null) {
			return parseTimeZone(node.getTextContent());
		}
		return default_value;
	}
	
	public TimeZone parseTimeZone (String b64) {
		// Doc : [MS-ASDTYPE] 2.7 TimeZone
		// Doc about types : http://msdn.microsoft.com/fr-fr/library/bb469811.aspx
		//      1 LONG = 4 bytes
		//      1 WCHAR = 2 bytes
		//      1 SYSTEMTIME = 8 SHORT = 8 X 2 bytes
		//      TOTAL TIMEZONE STRUCT must be 172 bytes
		
		byte tzstruct[] = Base64.decode(b64.toCharArray());

		ByteBuffer bfBias = ByteBuffer.wrap(tzstruct, 0, 4);
//		NOT YET USED
//
//		ByteBuffer bfStandardName = ByteBuffer.wrap(tzstruct, 4, 64);
//		ByteBuffer bfStandardDate = ByteBuffer.wrap(tzstruct, 68, 16);
//		ByteBuffer bfStandardBias = ByteBuffer.wrap(tzstruct, 84, 4);
//		ByteBuffer bfDaylightName = ByteBuffer.wrap(tzstruct, 88, 64);
//		ByteBuffer bfDaylightDate = ByteBuffer.wrap(tzstruct, 152, 16);
//		ByteBuffer bfDaylightBias = ByteBuffer.wrap(tzstruct, 168, 4);
		
		bfBias.order(ByteOrder.LITTLE_ENDIAN);
		int bias = bfBias.getInt(); // Java integer is 4-bytes-long
		
//		NOT YET USED
//
//		bfStandardBias.order(ByteOrder.LITTLE_ENDIAN);
//		int standardBias = bfStandardBias.getInt();
//		
//		bfDaylightBias.order(ByteOrder.LITTLE_ENDIAN);
//		int daylightBias = bfDaylightBias.getInt();
		
		TimeZone timezone = TimeZone.getDefault();
		timezone.setRawOffset(bias * 60 * 1000);
		
		String timezones[] = TimeZone.getAvailableIDs(bias * 60 * 1000);
		if (timezones.length > 0) {
			timezone = TimeZone.getTimeZone(timezones[0]);
		}

//		USEFUL DEBUG LINES
//
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < 172; i+=1) {
//			sb.append(Byte.valueOf(tzstruct[i]).intValue());
//		}
//		
//		logger.info("b64: " + b64);
//		logger.info("tzstruct: "+ sb.toString());
//		logger.info("bias: " + bias);
//		logger.info("standardbias: " + standardBias);
//		logger.info("standardname: " + bfStandardName.asCharBuffer().toString());
//		logger.info("daylightBias: " + daylightBias);
		
		return timezone;
	}
	
	public ArrayList<String> parseDOMStringCollection(Element node, String elementName, ArrayList<String> default_value) {
		if (node != null) { 
			return new ArrayList<String>(Arrays.asList(DOMUtils.getTexts(node, elementName)));
		}
		
		return default_value;
	}
	
	public ArrayList<String> parseDOMStringCollection(Element node, String elementName) {
		return parseDOMStringCollection(node, elementName, null);
	}
	
	public byte parseByte (String str) {
		return Byte.parseByte(str);
	}
	
	public int parseInt (String str) {
		return Integer.parseInt(str);
	}
	
	public boolean parseBoolean (String str) {
		return Boolean.parseBoolean(str);
	}
	
	public Boolean parseDOMBoolean (Element elt, Boolean default_value) {
		if (elt != null) {
			return parseBoolean(elt.getTextContent());
		}
		return default_value;
	}
	
	public Boolean parseDOMBoolean (Element elt) {
		return parseDOMBoolean(elt, null);
	}
	
	/**
	 * Return an int else -1
	 * @param elt
	 * @return int
	 */
	public int parseDOMNoNullInt (Element elt) {
		if (elt == null)
			return -1;
		
		return Integer.parseInt(elt.getTextContent());
	}
	
	/**
	 * Return true if 1 else false
	 * @param elt
	 * @return
	 */
	public Boolean parseDOMInt2Boolean (Element elt) {
		if (parseDOMNoNullInt(elt) == 1)
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}
}
