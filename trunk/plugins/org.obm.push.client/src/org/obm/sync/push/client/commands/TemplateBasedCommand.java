package org.obm.sync.push.client.commands;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.IEasCommand;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Document;

public abstract class TemplateBasedCommand<T> implements IEasCommand<T> {

	protected Document tpl;
	protected Log logger = LogFactory.getLog(getClass());
	private String namespace;
	private String cmd;

	protected TemplateBasedCommand(NS namespace, String cmd, String templateName) {
		this.namespace = namespace.toString();
		this.cmd = cmd;
		InputStream in = loadDataFile(templateName);
		if (in != null) {
			try {
				this.tpl = DOMUtils.parse(in);
			} catch (Exception e) {
				logger.error("error loading template "+templateName, e);
			}
		} else {
			logger.error("template "+templateName+" not found.");
		}
	}

	@Override
	public T run(AccountInfos ai, OPClient opc, HttpClient hc)
			throws Exception {
		customizeTemplate(ai, opc);
		Document response = opc.postXml(namespace, tpl, cmd);
		T ret = parseResponse(response);
		return ret;
	}

	protected abstract void customizeTemplate(AccountInfos ai, OPClient opc);

	protected abstract T parseResponse(Document response);

	private InputStream loadDataFile(String name) {
		return TemplateBasedCommand.class.getClassLoader().getResourceAsStream(
				"data/" + name);
	}

}
