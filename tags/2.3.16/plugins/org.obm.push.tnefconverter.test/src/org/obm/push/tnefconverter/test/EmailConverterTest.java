package org.obm.push.tnefconverter.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.obm.push.tnefconverter.EmailConverter;
import org.obm.push.tnefconverter.TNEFConverterException;
import org.obm.push.utils.FileUtils;


import junit.framework.TestCase;

public class EmailConverterTest extends TestCase {
	
	public void testConvert() throws TNEFConverterException, IOException {
		InputStream eml = loadDataFile("fgggh.eml");
		assertNotNull(eml);
		String s = FileUtils.streamString(eml, true);
		InputStream in = new EmailConverter().convert(new ByteArrayInputStream(s.getBytes()));
		assertNotNull(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileUtils.transfer(in, out, true);
		byte[] data = out.toByteArray();
		System.out.println(new String(data));
		
	}

	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"data/eml/" + name);
	}
}
