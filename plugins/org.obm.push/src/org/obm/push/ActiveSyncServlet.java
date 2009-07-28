package org.obm.push;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IBackendFactory;
import org.obm.push.impl.Credentials;
import org.obm.push.impl.FolderSyncHandler;
import org.obm.push.impl.GetItemEstimateHandler;
import org.obm.push.impl.HintsLoader;
import org.obm.push.impl.IRequestHandler;
import org.obm.push.impl.PingHandler;
import org.obm.push.impl.ProvisionHandler;
import org.obm.push.impl.Responder;
import org.obm.push.impl.SearchHandler;
import org.obm.push.impl.SendMailHandler;
import org.obm.push.impl.SettingsHandler;
import org.obm.push.impl.SyncHandler;
import org.obm.push.store.IStorageFactory;
import org.obm.push.store.ISyncStorage;
import org.obm.push.utils.Base64;
import org.obm.push.utils.RunnableExtensionLoader;

/**
 * ActiveSync server implementation. Routes all request to appropriate request
 * handlers.
 * 
 * @author tom
 * 
 */
public class ActiveSyncServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4136686109306545436L;

	private static final Log logger = LogFactory
			.getLog(ActiveSyncServlet.class);

	private Map<String, IRequestHandler> handlers;
	private Map<String, BackendSession> sessions;

	private ISyncStorage storage;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Continuation c = ContinuationSupport.getContinuation(request, request);

		logger.info(" uri: " + request.getRequestURI() + " "
				+ request.getQueryString() + " pending => " + c.isPending()
				+ " resumed => " + c.isResumed() + " method: "
				+ request.getMethod());

		if (c.isResumed()) {
			PingHandler ph = (PingHandler) handlers.get("Ping");
			ph.sendResponse((BackendSession) c.getObject(), new Responder(
					response));
			return;
		}

		String m = request.getMethod();
		if ("OPTIONS".equals(m)) {
			sendOptionsResponse(response);
			return;
		}

		Credentials creds = performAuthentification(request, response);
		if (creds == null) {
			return;
		}

		String policy = p(request, "X-Ms-PolicyKey");
		if (policy != null && policy.equals("0")
				&& !p(request, "Cmd").equals("Provision")) {
			// force device provisioning
			logger.info("[" + creds.getLoginAtDomain()
					+ "] forcing device (ua: " + p(request, "User-Agent")
					+ ") provisioning ");
			response.setStatus(449);
			return;
		} else {
			logger.info("[" + creds.getLoginAtDomain() + "] policy used: "
					+ policy);
		}

		processActiveSyncMethod(c, creds.getLoginAtDomain(), creds
				.getPassword(), request, response);

	}

	/**
	 * Checks authentification headers. Returns non null value if login/password
	 * is valid & the device has been authorized.
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private Credentials performAuthentification(HttpServletRequest request,
			HttpServletResponse response) {
		Credentials creds = null;
		boolean valid = false;
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();
				if (basic.equalsIgnoreCase("Basic")) {
					String credentials = st.nextToken();
					String userPass = new String(Base64.decode(credentials
							.toCharArray()));
					int p = userPass.indexOf(":");
					if (p != -1) {
						String userId = userPass.substring(0, p);
						String password = userPass.substring(p + 1);
						String loginAtDomain = getLoginAtDomain(userId);
						valid = validatePassword(loginAtDomain, password);
						valid = valid
								&& storage
										.initDevice(loginAtDomain, p(request,
												"DeviceId"),
												extractDeviceType(request));
						if (valid) {
							creds = new Credentials(loginAtDomain, password);
						}
					}
				}
			}
		}

		if (!valid) {
			String uri = request.getMethod() + " " + request.getRequestURI()
					+ " " + request.getQueryString();
			logger.warn("invalid auth, sending http 401 (uri: " + uri + ")");
			String s = "Basic realm=\"OBMPushService\"";
			response.setHeader("WWW-Authenticate", s);
			response.setStatus(401);
		}
		return creds;
	}

	private String extractDeviceType(HttpServletRequest request) {
		String ret = p(request, "DeviceType");
		if (ret.startsWith("IMEI")) {
			ret = p(request, "User-Agent");
		}
		return ret;
	}

	/**
	 * Parameters can be in query string or in header, whether a base64 query
	 * string is used.
	 * 
	 * @param r
	 * @param name
	 * @return
	 */
	private String p(HttpServletRequest r, String name) {
		String ret = null;
		String qs = r.getQueryString();
		if (qs.contains("User=")) {
			ret = r.getParameter(name);
		} else {
			Base64QueryString bqs = new Base64QueryString(qs);
			ret = bqs.getValue(name);
		}
		if (ret == null) {
			ret = r.getHeader(name);
		}
		return ret;
	}

	private void processActiveSyncMethod(Continuation continuation,
			String userID, String password, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		BackendSession bs = getSession(userID, password, request);
		logger.info("activeSyncMethod: " + bs.getCommand());
		String proto = p(request, "MS-ASProtocolVersion");
		bs.setProtocolVersion(Double.parseDouble(proto));
		logger.info("Client supports protocol " + proto);

		if (bs.getCommand() == null) {
			logger.warn("POST received without explicit command, aborting");
			return;
		}

		IRequestHandler rh = getHandler(bs);
		if (rh == null) {
			noHandlerError(request, bs);
			return;
		}

		InputStream in = request.getInputStream();
		sendASHeaders(response);
		rh.process(continuation, bs, in, new Responder(response));
	}

	@SuppressWarnings("unchecked")
	private void noHandlerError(HttpServletRequest request, BackendSession bs) {
		logger.warn("no handler for command " + bs.getCommand());
		Enumeration heads = request.getHeaderNames();
		while (heads.hasMoreElements()) {
			String h = (String) heads.nextElement();
			logger.warn(h + ": " + request.getHeader(h));
		}
	}

	private BackendSession getSession(String userID, String password,
			HttpServletRequest r) {
		String uid = getLoginAtDomain(userID);

		BackendSession bs = null;
		if (sessions.containsKey(uid)) {
			bs = sessions.get(uid);
			bs.setCommand(p(r, "Cmd"));
		} else {
			bs = new BackendSession(uid, password, p(r, "DeviceId"),
					extractDeviceType(r), p(r, "Cmd"));
			new HintsLoader().addHints(r, bs);
		}
		bs.setRequest(r);
		return bs;
	}

	private String getLoginAtDomain(String userID) {
		String uid = userID;
		String domain = null;
		int idx = uid.indexOf("\\");
		if (idx > 0) {
			domain = uid.substring(0, idx);
			if (!uid.contains("@")) {
				uid = uid.substring(idx + 1) + "@" + domain;
			} else {
				uid = uid.substring(idx + 1);
			}
		}
		uid = uid.toLowerCase();
		logger.info("loginAtDomain: " + uid + " domain: " + domain);
		return uid;
	}

	private void sendASHeaders(HttpServletResponse response) {
		// HTTP/1.1 200 OK
		// Connection: Keep-Alive
		// Content-Length: 1069
		// Date: Mon, 01 May 2006 20:15:15 GMT
		// Content-Type: application/vnd.ms-sync.wbxml
		// Server: Microsoft-IIS/6.0
		// X-Powered-By: ASP.NET
		// X-AspNet-Version: 2.0.50727
		// MS-Server-ActiveSync: 8.0
		// Cache-Control: private

		response.setHeader("Server", "Microsoft-IIS/6.0");
		response.setHeader("MS-Server-ActiveSync", "8.1");
		response.setHeader("Cache-Control", "private");
	}

	private void sendOptionsResponse(HttpServletResponse response) {
		response.setStatus(200);
		response.setHeader("Server", "Microsoft-IIS/6.0");
		response.setHeader("MS-Server-ActiveSync", "8.1");
		response
				.setHeader("MS-ASProtocolVersions", "1.0,2.0,2.1,2.5,12.0,12.1");
		response
				.setHeader(
						"MS-ASProtocolCommands",
						"Sync,SendMail,SmartForward,SmartReply,GetAttachment,GetHierarchy,CreateCollection,DeleteCollection,MoveCollection,FolderSync,FolderCreate,FolderDelete,FolderUpdate,MoveItems,GetItemEstimate,MeetingResponse,Search,Settings,Ping,ItemOperations,Provision,ResolveRecipients,ValidateCert");
		response.setHeader("Public", "OPTIONS,POST");
		response.setHeader("Allow", "OPTIONS,POST");
		response.setHeader("Cache-Control", "private");
		response.setContentLength(0);
	}

	private IRequestHandler getHandler(BackendSession p) {
		return handlers.get(p.getCommand());
	}

	private boolean validatePassword(String userID, String password) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void init() throws ServletException {
		super.init();

		PushConfiguration pc = new PushConfiguration();

		IBackend backend = loadBackend(pc);

		sessions = new HashMap<String, BackendSession>();

		handlers = new HashMap<String, IRequestHandler>();
		handlers.put("FolderSync", new FolderSyncHandler(backend));
		handlers.put("Sync", new SyncHandler(backend));
		handlers.put("GetItemEstimate", new GetItemEstimateHandler(backend));
		handlers.put("Provision", new ProvisionHandler(backend));
		handlers.put("Ping", new PingHandler(backend));
		handlers.put("Settings", new SettingsHandler(backend));
		handlers.put("Search", new SearchHandler(backend));
		handlers.put("SendMail", new SendMailHandler(backend));

		System.out.println("ActiveSync servlet initialised.");
	}

	private IBackend loadBackend(PushConfiguration pc) {
		RunnableExtensionLoader<IStorageFactory> sto = new RunnableExtensionLoader<IStorageFactory>();
		List<IStorageFactory> storages = sto.loadExtensions("org.obm.push",
				"storage", "storage", "implementation");
		if (storages.size() > 0) {
			IStorageFactory stoFactoryImpl = storages.get(0);
			storage = stoFactoryImpl.createStorage();

		} else {
			logger.error("No storage implementation found");
			return null;
		}

		RunnableExtensionLoader<IBackendFactory> rel = new RunnableExtensionLoader<IBackendFactory>();
		List<IBackendFactory> backs = rel.loadExtensions("org.obm.push",
				"backend", "backend", "implementation");
		if (backs.size() > 0) {
			IBackendFactory bf = backs.get(0);
			return bf.loadBackend(storage, pc);
		} else {
			logger.error("No push backend found.");
			return null;
		}

	}

}