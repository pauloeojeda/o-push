package org.obm.push.utils;

import net.freeutils.tnef.TNEFUtils;

public class GlobalObjectIdUtils {

//	private static final Log logger = LogFactory
//			.getLog(GlobalObjectIdUtils.class);

	/**
	 * http://msdn.microsoft.com/en-us/library/ee160198(EXCHG.80).aspx
	 * Spec MS-OXOCAL 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static String getUid(byte[] data) throws Exception {

		String sdata = TNEFUtils.toHexString(data);
		// Byte Array ID (16 bytes): An array of 16 bytes identifying this BLOB
		// as a Global Object ID.
		// The byte array MUST be as follows:
		String goiID = "040000008200E00074C5B7101A82E008";
		if (!sdata.startsWith(goiID)) {
			throw new IllegalArgumentException();
		}

		// Size (4 bytes): A LONG value that defines the size of the Data
		// component.
		String uidLabel = new String(getHexa(sdata.substring(80, 96)));
		if (!"vCal-Uid".equalsIgnoreCase(uidLabel)) {
			return sdata;
		}

		return new String(sdata.substring(104, sdata.length() - 2));

	}

	public static char[] getHexa(String hexa) {
		if (hexa.length() % 2 != 0) {
			throw new RuntimeException("Taille incorrecte");
		}
		char[] buf = new char[hexa.length() / 2];
		int i = 0;
		for (int pos = 0; pos < hexa.length(); pos += 2) {
			String substring = hexa.substring(pos, pos + 2);
			buf[i++] = (char) Integer.decode("0x" + substring).intValue();
		}
		return buf;
	}
}
