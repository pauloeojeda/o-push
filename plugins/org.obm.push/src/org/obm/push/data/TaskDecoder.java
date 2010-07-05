package org.obm.push.data;


import org.obm.push.backend.IApplicationData;
import org.obm.push.backend.MSTask;
import org.w3c.dom.Element;

public class TaskDecoder extends Decoder implements IDataDecoder {

	@Override
	public IApplicationData decode(Element syncData) {
		MSTask task = new MSTask();

		return task;
	}

}
