package org.obm.push.storage.jdbc;

import org.obm.push.store.IStorageFactory;
import org.obm.push.store.ISyncStorage;

public class StorageFactory implements IStorageFactory {

	@Override
	public ISyncStorage createStorage() {
		return new SyncStorage();
	}

}
