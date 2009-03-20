package org.obm.push.data;

import org.obm.push.backend.IApplicationData;
import org.w3c.dom.Element;

public interface IDataEncoder {

	void encode(Element parent, IApplicationData data);

}
