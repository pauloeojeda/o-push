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
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.obm.push.utils.Base64;
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
	private String login;
	private String userAgent;
	private String password;

	protected AbstractPushTest() {
		XTrustProvider.install();
	}

	// "POST /Microsoft-Server-ActiveSync?User=thomas@zz.com&DeviceId=Appl87837L1XY7H&DeviceType=iPhone&Cmd=Sync HTTP/1.1"

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();

		// this.userId = "thomas@zz.com";
		// this.devId = "junitDevId";
		// this.devType = "PocketPC";
		// this.url = "https://10.0.0.5/Microsoft-Server-ActiveSync";

		this.login = "Administrator";
		this.userId = "test.tlse.lng\\Administrator";
		this.password = "aliacom";
		this.devId = "Appl8683191J1R4";
		this.devType = "iPhone";
		this.userAgent = "Apple-iPhone/701.341";
		this.url = "http://2k3.test.tlse.lng/Microsoft-Server-ActiveSync";

		this.hc = createHttpClient();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private HttpClient createHttpClient() {
		HttpClient ret = new HttpClient(
				new MultiThreadedHttpConnectionManager());
		HttpConnectionManagerParams mp = ret.getHttpConnectionManager()
				.getParams();
		mp.setDefaultMaxConnectionsPerHost(4);
		mp.setMaxTotalConnections(8);

		// ret.getState().setCredentials(new AuthScope("10.0.0.5", 443,
		// "ZPush"),
		// new UsernamePasswordCredentials(userId, "aliacom"));

		// ret.getState().setCredentials(
		// new AuthScope("2k3.test.tlse.lng", 80, "2k3.test.tlse.lng"),
		// new UsernamePasswordCredentials(userId, "aliacom"));

		return ret;
	}

	protected InputStream loadDataFile(String name) {
		return AbstractPushTest.class.getClassLoader().getResourceAsStream(
				"data/" + name);
	}

	private String authValue() {
		StringBuilder sb = new StringBuilder();
		sb.append("Basic ");
		String encoded = new String(Base64.encode((userId + ":" + password)
				.getBytes()));
		sb.append(encoded);
		String ret = sb.toString();
		System.err.println("authString: " + ret);
		return ret;
	}

	protected void optionsQuery() throws Exception {
		OptionsMethod pm = new OptionsMethod(url + "?User=" + login
				+ "&DeviceId=" + devId + "&DeviceType=" + devType);
		pm.setRequestHeader("User-Agent", userAgent);
		pm.setRequestHeader("Authorization", authValue());
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
	}

	protected Document postXml(String namespace, Document doc, String cmd)
			throws Exception {
		return postXml(namespace, doc, cmd, null, "12.1");
	}

	@SuppressWarnings("deprecation")
	protected Document postXml(String namespace, Document doc, String cmd,
			String policyKey, String protocolVersion) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] data = WBXMLTools.toWbxml(namespace, doc);
		PostMethod pm = new PostMethod(url + "?User=" + login + "&DeviceId="
				+ devId + "&DeviceType=" + devType + "&Cmd=" + cmd);
		pm.setRequestHeader("Content-Length", "" + data.length);
		pm.setRequestBody(new ByteArrayInputStream(data));
		pm.setRequestHeader("Content-Type", "application/vnd.ms-sync.wbxml");
		pm.setRequestHeader("Authorization", authValue());
		pm.setRequestHeader("User-Agent", userAgent);
		pm.setRequestHeader("Ms-Asprotocolversion", protocolVersion);
		pm.setRequestHeader("Accept", "*/*");
		pm.setRequestHeader("Accept-Language", "fr-fr");
		pm.setRequestHeader("Connection", "keep-alive");
		// pm.setRequestHeader("Accept-Encoding", "gzip, deflate");
		if (policyKey != null) {
			pm.setRequestHeader("X-MS-PolicyKey", policyKey);
		}

		Document xml = null;
		synchronized (hc) {
			try {
				int ret = hc.executeMethod(pm);
				Header[] hs = pm.getResponseHeaders();
				for (Header h : hs) {
					System.err.println("head[" + h.getName() + "] => "
							+ h.getValue());
				}
				if (ret != HttpStatus.SC_OK) {
					System.err.println("method failed:\n" + pm.getStatusLine()
							+ "\n" + pm.getResponseBodyAsString());
				} else {
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
