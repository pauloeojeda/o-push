package org.obm.push.data;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IApplicationData;
import org.w3c.dom.Element;

public interface IDataEncoder {

	void encode(BackendSession bs, Element parent, IApplicationData data, boolean truncation, boolean isResponse);

}
