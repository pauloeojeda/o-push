package org.obm.push.client.tests;

import java.util.Map;

import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.SyncResponse;
import org.w3c.dom.Document;

public class OPClientTests extends AbstractPushTest {

	public void testOptions() {
		try {
			opc.options();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public FolderSyncResponse testInitialFolderSync() {
		try {
			FolderSyncResponse resp = opc.folderSync("0");
			assertNotNull(resp);
			assertNotNull(resp.getFolders());
			assertTrue(resp.getFolders().size() > 0);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}

	public SyncResponse testInitialSync(Folder... folders) {
		try {
			SyncResponse resp = opc.initialSync(folders);
			assertNotNull(resp);
			assertNotNull(resp.getCollections());
			assertEquals(folders.length, resp.getCollections().size());
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}
	
	public SyncResponse testSync(Document doc) {
		try {
			SyncResponse resp = opc.sync(doc);
			assertNotNull(resp);
			assertNotNull(resp.getCollections());
			assertTrue(resp.getCollections().size()>0);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		return null;
	}
	
	public String getSyncKey(String collectionId, Map<String, Collection> cols){
		Collection col = cols.get(collectionId);
		assertNotNull("Collection["+collectionId+"] not found",col);
		assertNotNull(col.getSyncKey());
		return col.getSyncKey();
	}
}
