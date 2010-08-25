package org.obm.sync.push.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AbstractPushTest extends TestCase {

	private String userId;
	private String devId;
	private String devType;
	private String url;
	private HttpClient hc;
	private String login;
	private String userAgent;
	private String password;
	private MultiThreadedHttpConnectionManager mtManager;

	protected AbstractPushTest() {
		XTrustProvider.install();
	}

	// "POST /Microsoft-Server-ActiveSync?User=thomas@zz.com&DeviceId=Appl87837L1XY7H&DeviceType=iPhone&Cmd=Sync HTTP/1.1"

	private String p(Properties p, String k) {
		return p.getProperty(k);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		InputStream in = loadDataFile("test.properties");
		Properties p = new Properties();
		p.load(in);
		in.close();

		this.login = p(p, "login");
		this.userId = p(p, "userId");
		this.password = p(p, "password");
		this.devId = p(p, "devId");
		this.devType = p(p, "devType");
		this.userAgent = p(p, "userAgent");
		this.url = p(p, "url");

		System.err.println("l: " + login + " u: " + userId + " p: " + password
				+ " di: " + devId + " dt: " + devType + " ua: " + userAgent
				+ " url: " + url);

		this.hc = createHttpClient();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		mtManager.shutdown();
	}

	private HttpClient createHttpClient() {
		this.mtManager = new MultiThreadedHttpConnectionManager();
		HttpClient ret = new HttpClient(mtManager);
		HttpConnectionManagerParams mp = ret.getHttpConnectionManager()
				.getParams();
		mp.setDefaultMaxConnectionsPerHost(8);
		mp.setMaxTotalConnections(16);
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
		return postXml(namespace, doc, cmd, null, "12.1", false);
	}

	protected Document postXml120(String namespace, Document doc, String cmd)
			throws Exception {
		return postXml(namespace, doc, cmd, null, "12.0", false);
	}

	protected Document postXml25(String namespace, Document doc, String cmd)
			throws Exception {
		return postXml(namespace, doc, cmd, null, "2.5", false);
	}

	@SuppressWarnings("deprecation")
	protected Document postXml(String namespace, Document doc, String cmd,
			String policyKey, String protocolVersion, boolean multipart)
			throws Exception {
		System.out.println("To server:");
		DOMUtils.logDom(doc);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] data = WBXMLTools.toWbxml(namespace, doc);
		PostMethod pm = null;
		pm = new PostMethod(url + "?User=" + login + "&DeviceId=" + devId
				+ "&DeviceType=" + devType + "&Cmd=" + cmd);
		pm.setRequestHeader("Content-Length", "" + data.length);
		pm.setRequestBody(new ByteArrayInputStream(data));
		pm.setRequestHeader("Content-Type", "application/vnd.ms-sync.wbxml");
		pm.setRequestHeader("Authorization", authValue());
		pm.setRequestHeader("User-Agent", userAgent);
		pm.setRequestHeader("Ms-Asprotocolversion", protocolVersion);
		pm.setRequestHeader("Accept", "*/*");
		pm.setRequestHeader("Accept-Language", "fr-fr");
		pm.setRequestHeader("Connection", "keep-alive");
		if (multipart) {
			pm.setRequestHeader("MS-ASAcceptMultiPart", "T");
			pm.setRequestHeader("Accept-Encoding", "gzip");
		}

		if (policyKey != null) {
			pm.setRequestHeader("X-MS-PolicyKey", policyKey);
		}

		Document xml = null;
		try {
			int ret = 0;
			ret = hc.executeMethod(pm);
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
				FileUtils.transfer(is, new FileOutputStream(localCopy), true);
				System.out.println("binary response stored in "
						+ localCopy.getAbsolutePath());

				InputStream in = new FileInputStream(localCopy);
				if (pm.getResponseHeader("Content-Encoding") != null
						&& pm.getResponseHeader("Content-Encoding").getValue()
								.contains("gzip")) {
					in = new GZIPInputStream(in);
				}
				out = new ByteArrayOutputStream();
				FileUtils.transfer(in, out, true);
				if (pm.getResponseHeader("Content-Type") != null
						&& pm.getResponseHeader("Content-Type").getValue()
								.contains("application/vnd.ms-sync.multipart")) {
					byte[] all = out.toByteArray();
					int idx = 0;
					byte[] buffer = new byte[4];
					for (int i = 0; i < buffer.length; i++) {
						buffer[i] = all[idx++];
					}
					int nbPart = byteArrayToInt(buffer);

					for (int p = 0; p < nbPart; p++) {
						for (int i = 0; i < buffer.length; i++) {
							buffer[i] = all[idx++];
						}
						int start = byteArrayToInt(buffer);

						for (int i = 0; i < buffer.length; i++) {
							buffer[i] = all[idx++];
						}
						int length = byteArrayToInt(buffer);

						byte[] value = new byte[length];
						for (int j = 0; j < length; j++) {
							value[j] = all[start++];
						}
						if (p == 0) {
							xml = WBXMLTools.toXml(value);
							DOMUtils.logDom(xml);
						} else {
							String file = new String(value);
							System.out.println("File: " + file);
						}

					}
				} else if (out.toByteArray().length > 0) {
					xml = WBXMLTools.toXml(out.toByteArray());
					DOMUtils.logDom(xml);
				}
			}
		} finally {
			pm.releaseConnection();
		}
		return xml;
	}

	public static final int byteArrayToInt(byte[] b) {
		byte[] inverse = new byte[b.length];
		int in = b.length - 1;
		for (int i = 0; i < b.length; i++) {
			inverse[in--] = b[i];
		}
		return (inverse[0] << 24) + ((inverse[1] & 0xFF) << 16)
				+ ((inverse[2] & 0xFF) << 8) + (inverse[3] & 0xFF);
	}

	protected byte[] postGetAttachment(String attachmentName) throws Exception {
		return postGetAttachment(attachmentName, "12.1");
	}

	protected byte[] postGetAttachment(String attachmentName,
			String protocolVersion) throws Exception {
		PostMethod pm = new PostMethod(url + "?User=" + login + "&DeviceId="
				+ devId + "&DeviceType=" + devType
				+ "&Cmd=GetAttachment&AttachmentName=" + attachmentName);
		pm.setRequestHeader("Authorization", authValue());
		pm.setRequestHeader("User-Agent", userAgent);
		pm.setRequestHeader("Ms-Asprotocolversion", protocolVersion);
		pm.setRequestHeader("Accept", "*/*");
		pm.setRequestHeader("Accept-Language", "fr-fr");
		pm.setRequestHeader("Connection", "keep-alive");

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
					for (Header h : pm.getResponseHeaders()) {
						System.out.println(h.getName() + ": " + h.getValue());
					}
					InputStream is = pm.getResponseBodyAsStream();
					File localCopy = File.createTempFile("pushresp_", ".bin");
					FileUtils.transfer(is, new FileOutputStream(localCopy),
							true);
					System.out.println("binary response stored in "
							+ localCopy.getAbsolutePath());

					FileInputStream in = new FileInputStream(localCopy);
					return FileUtils.streamBytes(in, true);
				}
			} finally {
				pm.releaseConnection();
			}
		}
		return null;

	}

	protected void fillSyncKey(Element root, Map<String, String> sks) {
		NodeList nl = root.getElementsByTagName("Collection");

		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			String collectionId = DOMUtils.getElementText(col, "CollectionId");
			String syncKey = sks.get(collectionId);
			Element synckeyElem = DOMUtils.getUniqueElement(col, "SyncKey");
			if (synckeyElem == null) {
				synckeyElem = DOMUtils.getUniqueElement(col, "AirSync:SyncKey");
			}
			synckeyElem.setTextContent(syncKey);
		}

	}

	protected Map<String, String> processCollection(Element root) {
		Map<String, String> ret = new HashMap<String, String>();
		NodeList nl = root.getElementsByTagName("Collection");

		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			String collectionId = DOMUtils.getElementText(col, "CollectionId");
			String syncKey = DOMUtils.getElementText(col, "SyncKey");
			ret.put(collectionId, syncKey);
			System.out.println(collectionId + " " + syncKey);
		}
		return ret;
	}
}
