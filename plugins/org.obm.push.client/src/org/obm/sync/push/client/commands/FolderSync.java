package org.obm.sync.push.client.commands;

import java.util.LinkedList;

import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Document;

public class FolderSync extends TemplateBasedCommand<FolderSyncResponse> {

	public FolderSync(String syncKey) {
		super(NS.FolderHierarchy, "FolderSync", "FolderSyncRequest.xml");
	}

	@Override
	protected void customizeTemplate(AccountInfos ai, OPClient opc) {

	}

	@Override
	protected FolderSyncResponse parseResponse(Document response) {
		LinkedList<Folder> ret = new LinkedList<Folder>();
		String key = "returnedKey";

		// TODO Auto-generated method stub

		return new FolderSyncResponse(key, ret);
	}

}
