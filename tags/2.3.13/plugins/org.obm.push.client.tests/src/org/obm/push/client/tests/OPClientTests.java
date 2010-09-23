package org.obm.push.client.tests;

import org.obm.sync.push.client.FolderSyncResponse;

public class OPClientTests extends AbstractPushTest {
	
	public void testOptions() {
		try {
			opc.options();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testInitialFolderSync() {
		try {
			FolderSyncResponse resp = opc.folderSync("0");
			assertNotNull(resp);
			assertNotNull(resp.getFolders());
			assertTrue(resp.getFolders().size() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
