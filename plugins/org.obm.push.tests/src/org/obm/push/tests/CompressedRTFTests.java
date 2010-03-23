package org.obm.push.tests;

import java.io.ByteArrayInputStream;

import fr.aliasource.utils.FileUtils;

import net.freeutils.tnef.CompressedRTFInputStream;

import junit.framework.TestCase;



public class CompressedRTFTests extends TestCase {

	public void testDecompress() throws Exception {
		String rtf = "3gAAADkCAABMWkZ1lTR5wz8ACQMwAQMB9wKnAgBjaBEKw"
				+ "HNldALRcHJx4DAgVGFoA3ECgwBQ6wNUDzcyD9MyBgAGwwKDpxIBA+"
				+ "MReDA0EhUgAoArApEI5jsJbzAVwzEyvjgJtBdCCjIXQRb0ORIAHxeEGOEYExjgFcMyNTX/"
				+ "CbQaYgoyGmEaHBaKCaUa9v8c6woUG3YdTRt/Hwwabxbt/xyPF7gePxg4JY0YVyRMKR+"
				+ "dJfh9CoEBMAOyMTYDMYksgSc1AUAnNmYtQNY3GoAtkDktgTMtQAwBFy3QLX8KhX0wgA==";
		
		System.out.println("compressed len: "+rtf);
		byte[]  bin = Base64.decode(rtf.getBytes());
		
		ByteArrayInputStream in = new ByteArrayInputStream(bin);
		CompressedRTFInputStream cin = new CompressedRTFInputStream(in);
		
		String rtfDecompressed = FileUtils.streamString(cin, true);
		System.out.println("decompressed:\n"+rtfDecompressed);
	}

}
