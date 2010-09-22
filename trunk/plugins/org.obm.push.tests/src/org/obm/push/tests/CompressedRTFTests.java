package org.obm.push.tests;

import org.obm.push.utils.RTFUtils;

import junit.framework.TestCase;


public class CompressedRTFTests extends TestCase {

	public void testDecompress() throws Exception {
		String rtf = "3gAAADkCAABMWkZ1lTR5wz8ACQMwAQMB9wKnAgBjaBEKw"
				+ "HNldALRcHJx4DAgVGFoA3ECgwBQ6wNUDzcyD9MyBgAGwwKDpxIBA+"
				+ "MReDA0EhUgAoArApEI5jsJbzAVwzEyvjgJtBdCCjIXQRb0ORIAHxeEGOEYExjgFcMyNTX/"
				+ "CbQaYgoyGmEaHBaKCaUa9v8c6woUG3YdTRt/Hwwabxbt/xyPF7gePxg4JY0YVyRMKR+"
				+ "dJfh9CoEBMAOyMTYDMYksgSc1AUAnNmYtQNY3GoAtkDktgTMtQAwBFy3QLX8KhX0wgA==";

		String txt = RTFUtils.extractB64CompressedRTF(rtf);
		assertEquals("Pouic pouic\n", txt);
	}

}
