package org.obm.sync.push.client.commands;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.IEasCommand;
import org.obm.sync.push.client.OPClient;

public class Options implements IEasCommand<Void> {

	@Override
	public Void run(AccountInfos ai, OPClient opc, HttpClient hc) throws Exception {
		OptionsMethod pm = new OptionsMethod(ai.getUrl() + "?User=" + ai.getLogin()
				+ "&DeviceId=" + ai.getDevId() + "&DeviceType=" + ai.getDevType());
		pm.setRequestHeader("User-Agent", ai.getUserAgent());
		pm.setRequestHeader("Authorization", ai.authValue());
		synchronized (hc) {
			try {
				int ret = hc.executeMethod(pm);
				if (ret != HttpStatus.SC_OK) {
					System.err.println("method failed:\n" + pm.getStatusLine()
							+ "\n" + pm.getResponseBodyAsString());
				}
				Header[] hs = pm.getResponseHeaders();
				for (Header h : hs) {
					System.err.println("resp head[" + h.getName() + "] => "
							+ h.getValue());

				}
			} finally {
				pm.releaseConnection();
			}
		}
		return null;
	}

}
