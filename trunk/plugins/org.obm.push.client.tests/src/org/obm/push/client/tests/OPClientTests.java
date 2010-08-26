package org.obm.push.client.tests;

public class OPClientTests extends AbstractPushTest {
	
	public void testOptions() {
		try {
			opc.options();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
