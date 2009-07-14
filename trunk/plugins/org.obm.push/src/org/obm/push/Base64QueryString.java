package org.obm.push;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.utils.Base64;

public class Base64QueryString {

	/**
	 * <code>
	Size         Field              Description
	1 byte       Protocol version   An integer that specifies the version of the
	                                ActiveSync protocol that is being used. This value
	                                MUST be 121.
	1 byte       Command code       An integer that specifies the command (see table
	                                of command codes in section 2.2.1.1.1.2).
	2 bytes      Locale             An integer that specifies the locale of the
	                                language that is used for the response.
	1 byte       Device ID length   An integer that specifies the length of the device
	                                ID. A value of 0 indicates that the device ID field
	                                is absent.
	0 - 16 bytes Device ID          A string or a GUID that identifies the device. A
	                                Windows Mobile device will use a GUID.
	1 byte       Policy key length  An integer that specifies the length of the policy
	                                key. The only valid values are 0 or 4. A value of 0
	                                indicates that the policy key field is absent.
	0 or 4 bytes Policy key         An integer that indicates the state of policy
	                                settings on the client device.
	1 byte       Device type length An integer that specifies the length of the device
	                                type value.
	0 - 16 bytes Device type        A string that specifies the type of client device.
	                                For details, see section 2.2.1.1.1.3.
	Variable     Command parameters A set of parameters that varies depending on the
	                                command. Each parameter consists of a tag,
	                                </code>
	 */

	private static final Log logger = LogFactory
			.getLog(Base64QueryString.class);
	private byte[] data;
	private String protocolVersion;
	private String cmdCode;

	public Base64QueryString(String b64) {
		this.data = Base64.decode(b64.toCharArray());
		int i = 0;
		protocolVersion = "" + (((float) data[i++]) / 10.0); // 0
		logger.info("version: " + protocolVersion);
		cmdCode = "" + data[i++]; // 1
		logger.info("cmd: " + cmdCode);
		int locale = (data[i++] << 8) + data[i++]; // 2 3

		String devId = "unknownDevId";
		if (data[i] > 0) {
			logger.info("devId size: " + data[4] + " current pos: " + i);
			for (int j = i+1; j < i+1+data[i];j++) {
				logger.info("data["+j+"]: "+data[j]);
			}
			devId = new String(data, i + 1, data[i]); // 4
			i += data[i] + 1;
		}

		int policyKey = 0;
		if (data[i++] == 4) { // got a policy key
			policyKey = policyKey + (data[i++] << 24) + (data[i++] << 16)
					+ (data[i++] << 8) + (data[i++]);
		}
		String devType = new String(data, i + 1, data[i]);
		i += data[i] + 1;
		logger.info("protoVersion: " + protocolVersion + " cmd: " + cmdCode
				+ " locInt: " + locale + " devId: aGUID"  + " pKey: "
				+ policyKey + " type: " + devType);

		// TODO variable parts
	}

	public String getValue(String key) {

		if (key.equalsIgnoreCase("MS-ASProtocolVersion")) {
			return protocolVersion;
		} else {
			logger.warn("cannot fetch '" + key + "' in b64 query string");
		}

		return null;
	}
}
