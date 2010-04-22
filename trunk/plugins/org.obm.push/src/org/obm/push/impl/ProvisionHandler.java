package org.obm.push.impl;

import java.util.Random;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContinuation;
import org.obm.push.provisioning.Policy;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles the Provision cmd
 * 
 * @author tom
 * 
 */
public class ProvisionHandler extends WbxmlRequestHandler {

	private final static String DEFAULT_PKEY = "3378841480";
	private Random random;

	public ProvisionHandler(IBackend backend) {
		super(backend);
		this.random = new Random();
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		String policyType = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"PolicyType").getTextContent();
		Element pKeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"PolicyKey");
		String pKey = "0";
		if (pKeyElem != null) {
			pKey = pKeyElem.getTextContent();
		}
		logger.info("required policyType: " + policyType + " key: " + pKey);

		if ("0".equals(pKey)) {

			try {
				Document ret = DOMUtils.createDoc(null, "Provision");
				Element root = ret.getDocumentElement();
				DOMUtils.createElementAndText(root, "Status", "1");
				Element policies = DOMUtils.createElement(root, "Policies");
				Element policy = DOMUtils.createElement(policies, "Policy");
				DOMUtils.createElementAndText(policy, "PolicyType", policyType);
				DOMUtils.createElementAndText(policy, "Status", "1");
				// pKey = "" + Math.abs((random.nextInt() >> 2));
				pKey = DEFAULT_PKEY;
				DOMUtils.createElementAndText(policy, "PolicyKey", pKey);
				Element data = DOMUtils.createElement(policy, "Data");

				Policy pol = backend.getDevicePolicy(bs);
				pol.serialize(data);

				responder.sendResponse("Provision", ret);

			} catch (Exception e) {
				logger.error("Error creating provision response", e);
			}
		} else {
			try {
				Document ret = DOMUtils.createDoc(null, "Provision");
				Element root = ret.getDocumentElement();
				DOMUtils.createElementAndText(root, "Status", "1");
				Element policies = DOMUtils.createElement(root, "Policies");
				Element policy = DOMUtils.createElement(policies, "Policy");
				DOMUtils.createElementAndText(policy, "PolicyType", policyType);
				if (DEFAULT_PKEY.equals(pKey)) {
					DOMUtils.createElementAndText(policy, "Status", "1");
					pKey = "" + Math.abs((random.nextInt() >> 2));
					bs.setPolicyKey(pKey);
					DOMUtils.createElementAndText(policy, "PolicyKey", pKey);
				} else {
					// The client is acknowledging the wrong policy key.
					DOMUtils.createElementAndText(policy, "Status", "5");
				}

				responder.sendResponse("Provision", ret);

			} catch (Exception e) {
				logger.error("Error creating provision response", e);
			}

		}

	}

}
