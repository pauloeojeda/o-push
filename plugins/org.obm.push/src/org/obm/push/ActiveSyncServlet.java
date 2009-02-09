package org.obm.push;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.impl.ASParams;
import org.obm.push.impl.Base64;
import org.obm.push.impl.FileUtils;
import org.obm.push.wbxml.WBXMLTools;

public class ActiveSyncServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4136686109306545436L;

	private static final Log logger = LogFactory
			.getLog(ActiveSyncServlet.class);

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String userID = null;
		String password = null;
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
						userID = userPass.substring(0, p);
						password = userPass.substring(p + 1);
						valid = validatePassword(userID, password);
					}
				}
			}
		}

		if (!valid) {
			System.out.println("invalid auth, sending http 401");
			String s = "Basic realm=\"OBMPushService\"";
			response.setHeader("WWW-Authenticate", s);
			response.setStatus(401);
		} else {
			processActiveSyncMethod(userID, request, response);
		}
	}

	private String p(HttpServletRequest r, String name) {
		return r.getParameter(name);
	}

	private ASParams getParams(HttpServletRequest r) {
		ASParams ret = new ASParams(p(r, "UserId"), p(r, "DeviceId"), p(r,
				"DeviceType"), p(r, "Cmd"));
		return ret;
	}

	private void processActiveSyncMethod(String userID,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		ASParams p = getParams(request);
		String m = request.getMethod();
		logger.info("activeSyncMethod: " + p.getCommand() + " method: " + m);
		String proto = request.getHeader("MS-ASProtocolVersion");
		logger.info("Client supports protocol " + proto);

		if ("OPTIONS".equals(m)) {
			response.setHeader("MS-Server-ActiveSync", "6.5.7638.1");
			response.setHeader("MS-ASProtocolVersions", "1.0,2.0,2.1,2.5");
			response
					.setHeader(
							"MS-ASProtocolCommands",
							"Sync,SendMail,SmartForward,SmartReply,GetAttachment,GetHierarchy,CreateCollection,DeleteCollection,MoveCollection,FolderSync,FolderCreate,FolderDelete,FolderUpdate,MoveItems,GetItemEstimate,MeetingResponse,ResolveRecipipents,ValidateCert,Provision,Search,Ping");
		} else if ("POST".equals(m)) {
			if (p.getCommand() == null) {
				logger.warn("POST received without explicit command, aborting");
				return;
			}

			if (logger.isInfoEnabled()) {
				InputStream in = request.getInputStream();
				byte[] input = FileUtils.streamBytes(in, true);
				logger.info("input:\n" + new String(input));
				try {
					WBXMLTools.toXml(input);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			response.setHeader("MS-Server-ActiveSync", "6.5.7638.1");
			IRequestHandler rh = getHandler(p);
			if (rh != null) {
				rh.process(request, response);
			} else {
				logger.warn("no handler for command " + p.getCommand());
			}
		}
	}

	private IRequestHandler getHandler(ASParams p) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean validatePassword(String userID, String password) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		System.out.println("ActiveSync servlet initialised.");
	}

}
