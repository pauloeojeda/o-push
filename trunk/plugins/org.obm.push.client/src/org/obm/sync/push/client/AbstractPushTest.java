package org.obm.sync.push.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

public class AbstractPushTest extends TestCase {

	private String userId;
	private String devId;
	private String devType;
	private String url;
	private HttpClient hc;

	protected AbstractPushTest() {
		XTrustProvider.install();
	}

	// "POST /Microsoft-Server-ActiveSync?User=thomas@zz.com&DeviceId=Appl87837L1XY7H&DeviceType=iPhone&Cmd=Sync HTTP/1.1"

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

//		this.userId = "thomas@zz.com";
//		this.devId = "junitDevId";
//		this.devType = "PocketPC";
//		this.url = "https://10.0.0.5/Microsoft-Server-ActiveSync";

		this.userId = "Administrator";
		this.devId = "junitDevId";
		this.devType = "PocketPC";
		this.url = "http://10.0.0.247/Microsoft-Server-ActiveSync";

		this.hc = createHttpClient();
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

	private HttpClient createHttpClient() {
		HttpClient ret = new HttpClient(
				new MultiThreadedHttpConnectionManager());
		HttpConnectionManagerParams mp = ret.getHttpConnectionManager()
				.getParams();
		mp.setDefaultMaxConnectionsPerHost(4);
		mp.setMaxTotalConnections(8);
//		ret.getState().setCredentials(new AuthScope("10.0.0.5", 443, "ZPush"),
//				new UsernamePasswordCredentials(userId, "aliacom"));
		ret.getState().setCredentials(new AuthScope("10.0.0.247", 80, "10.0.0.247"),
				new UsernamePasswordCredentials(userId, "aliacom"));
		
		return ret;
	}

	protected InputStream loadDataFile(String name) {
		return AbstractPushTest.class.getClassLoader().getResourceAsStream(
				"data/" + name);
	}

	@SuppressWarnings("deprecation")
	protected Document postXml(String namespace, Document doc, String cmd)
			throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] data = WBXMLTools.toWbxml(namespace, doc);
		PostMethod pm = new PostMethod(url + "?User=" + userId + "&DeviceId="
				+ devId + "&DeviceType=" + devType + "&Cmd=" + cmd);
		pm.setRequestBody(new ByteArrayInputStream(data));
		pm.setRequestHeader("MS-Asprotocolversion", "2.5");
		pm.setRequestHeader("Content-Type", "application/vnd.ms-sync.wbxml");
		Document xml = null;
		synchronized (hc) {
			try {
				int ret = hc.executeMethod(pm);
				if (ret != HttpStatus.SC_OK) {
					System.err.println("method failed:\n" + pm.getStatusLine()
							+ "\n" + pm.getResponseBodyAsString());
				} else {
					Header[] hs = pm.getResponseHeaders();
					for (Header h : hs) {
						System.err.println("head["+h.getName()+"] => "+h.getValue());
					}
					InputStream is = pm.getResponseBodyAsStream();
					File localCopy = File.createTempFile("pushresp_", ".bin");
					FileUtils.transfer(is, new FileOutputStream(localCopy),
							true);
					System.out.println("binary response stored in "
							+ localCopy.getAbsolutePath());
					FileInputStream in = new FileInputStream(localCopy);
					out = new ByteArrayOutputStream();
					FileUtils.transfer(in, out, true);
					xml = WBXMLTools.toXml(out.toByteArray());
					DOMUtils.logDom(xml);
				}
			} finally {
				pm.releaseConnection();
			}
			return xml;
		}
	}
}
