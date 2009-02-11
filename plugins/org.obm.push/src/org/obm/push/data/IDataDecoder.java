package org.obm.push.data;

import org.obm.push.backend.IApplicationData;

public interface IDataDecoder {

	IApplicationData decode(String textContent);

}
