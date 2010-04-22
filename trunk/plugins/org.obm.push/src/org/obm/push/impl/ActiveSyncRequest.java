package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

public interface ActiveSyncRequest {
	String getParameter(String key);

	InputStream getInputStream() throws IOException;

	String getHeader(String name);
}
