package org.obm.sync.push.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.utils.Base64;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

public class OPClient {

	private String userId;
	private String devId;
	private String devType;
	private String url;
	private HttpClient hc;
	private String login;
	private String userAgent;
	private String password;
	private MultiThreadedHttpConnectionManager mtManager;
	private ProtocolVersion protocolVersion;

	protected Log logger = LogFactory.getLog(getClass());

	static {
		XTrustProvider.install();
	}

	public OPClient(String loginAtDomain, String password, String devId,
			String devType, String userAgent, String url) throws Exception {

		setProtocolVersion(ProtocolVersion.V121);
		this.login = loginAtDomain;

		int idx = login.indexOf('@');
		if (idx > 0) {
			String d = login.substring(idx + 1);
			this.userId = d + "\\" + login.substring(0, idx);
		} else {
			logger.warn("login needs to contain the @domain part: " + login);
		}

		this.password = password;
		this.devId = devId;
		this.devType = devType;
		this.userAgent = userAgent;
		this.url = url;

		if (logger.isDebugEnabled()) {
			logger.debug("l: " + login + " u: " + userId + " p: " + password
					+ " di: " + devId + " dt: " + devType + " ua: " + userAgent
					+ " url: " + url);
		}

		this.hc = createHttpClient();
	}

	public void destroy() throws Exception {
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

	public void options() throws Exception {
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

	public Document postXml(String namespace, Document doc, String cmd)
			throws Exception {
		return postXml(namespace, doc, cmd, null, false);
	}

	@SuppressWarnings("deprecation")
	public Document postXml(String namespace, Document doc, String cmd,
			String policyKey, boolean multipart) throws Exception {

		DOMUtils.logDom(doc);

		byte[] data = WBXMLTools.toWbxml(namespace, doc);
		PostMethod pm = null;
		pm = new PostMethod(url + "?User=" + login + "&DeviceId=" + devId
				+ "&DeviceType=" + devType + "&Cmd=" + cmd);
		pm.setRequestHeader("Content-Length", "" + data.length);
		pm.setRequestBody(new ByteArrayInputStream(data));
		pm.setRequestHeader("Content-Type", "application/vnd.ms-sync.wbxml");
		pm.setRequestHeader("Authorization", authValue());
		pm.setRequestHeader("User-Agent", userAgent);
		pm.setRequestHeader("Ms-Asprotocolversion", protocolVersion.toString());
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
				ByteArrayOutputStream out = new ByteArrayOutputStream();
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

	private final int byteArrayToInt(byte[] b) {
		byte[] inverse = new byte[b.length];
		int in = b.length - 1;
		for (int i = 0; i < b.length; i++) {
			inverse[in--] = b[i];
		}
		return (inverse[0] << 24) + ((inverse[1] & 0xFF) << 16)
				+ ((inverse[2] & 0xFF) << 8) + (inverse[3] & 0xFF);
	}

	public byte[] postGetAttachment(String attachmentName) throws Exception {
		PostMethod pm = new PostMethod(url + "?User=" + login + "&DeviceId="
				+ devId + "&DeviceType=" + devType
				+ "&Cmd=GetAttachment&AttachmentName=" + attachmentName);
		pm.setRequestHeader("Authorization", authValue());
		pm.setRequestHeader("User-Agent", userAgent);
		pm.setRequestHeader("Ms-Asprotocolversion", protocolVersion.toString());
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

	public ProtocolVersion getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(ProtocolVersion protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
}
