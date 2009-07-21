package org.obm.push.impl;

import java.util.Random;

import org.mortbay.util.ajax.Continuation;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
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

	private Random random;

	public ProvisionHandler(IBackend backend) {
		super(backend);
		this.random = new Random();
	}

	@Override
	public void process(Continuation continuation, BackendSession bs,
			Document doc, Responder responder) {
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
				pKey = "3378841480";
				DOMUtils.createElementAndText(policy, "PolicyKey", pKey);
				Element data = DOMUtils.createElement(policy, "Data");

				Element provDoc = DOMUtils.createElement(data,
						"EASProvisionDoc");

				Policy pol = backend.getDevicePolicy(bs);
				serializePolicy(provDoc, pol);

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
				DOMUtils.createElementAndText(policy, "Status", "1");
				pKey = "" + Math.abs((random.nextInt() >> 2));
				bs.setPolicyKey(pKey);
				DOMUtils.createElementAndText(policy, "PolicyKey", pKey);

				responder.sendResponse("Provision", ret);

			} catch (Exception e) {
				logger.error("Error creating provision response", e);
			}

		}

	}

	private void p(Element provDoc, String field, String value) {
		DOMUtils.createElementAndText(provDoc, field, value);
	}

	private void serializePolicy(Element provDoc, Policy p) {
		p(provDoc, "DevicePasswordEnabled", "0");
		p(provDoc, "AlphanumericDevicePasswordRequired", "0");

		p(provDoc, "PasswordRecoveryEnabled", "0");
		p(provDoc, "DeviceEncryptionEnabled", "0");
		p(provDoc, "AttachmentsEnabled", "1");
		p(provDoc, "MinDevicePasswordLength", "4");

		p(provDoc, "MaxInactivityTimeDeviceLock", "900");
		p(provDoc, "MaxDevicePasswordFailedAttempts", "8");
		DOMUtils.createElement(provDoc, "MaxAttachmentSize");

		p(provDoc, "AllowSimpleDevicePassword", "1");
		DOMUtils.createElement(provDoc, "DevicePasswordExpiration");
		p(provDoc, "DevicePasswordHistory", "0");
		p(provDoc, "AllowStorageCard", "1");
		p(provDoc, "AllowCamera", "1");
		p(provDoc, "RequireDeviceEncryption", "0");
		p(provDoc, "AllowUnsignedApplications", "1");
		p(provDoc, "AllowUnsignedInstallationPackages", "1");

		p(provDoc, "MinDevicePasswordComplexCharacters", "3");
		p(provDoc, "AllowWiFi", "1");
		p(provDoc, "AllowTextMessaging", "1");
		p(provDoc, "AllowPOPIMAPEmail", "1");
		p(provDoc, "AllowBluetooth", "2");
		p(provDoc, "AllowIrDA", "1");
		p(provDoc, "RequireManualSyncWhenRoaming", "0");
		p(provDoc, "AllowDesktopSync", "1");
		p(provDoc, "MaxCalendarAgeFilter", "0");
		p(provDoc, "AllowHTMLEmail", "1");
		p(provDoc, "MaxEmailAgeFilter", "0");
		p(provDoc, "MaxEmailBodyTruncationSize", "-1");
		p(provDoc, "MaxEmailHTMLBodyTruncationSize", "-1");

		p(provDoc, "RequireSignedSMIMEMessages", "0");
		p(provDoc, "RequireEncryptedSMIMEMessages", "0");
		p(provDoc, "RequireSignedSMIMEAlgorithm", "0");
		p(provDoc, "RequireEncryptionSMIMEAlgorithm", "0");
		p(provDoc, "AllowSMIMEEncryptionAlgorithmNegotiation", "2");
		p(provDoc, "AllowSMIMESoftCerts", "1");
		p(provDoc, "AllowBrowser", "1");
		p(provDoc, "AllowConsumerEmail", "1");

		p(provDoc, "AllowRemoteDesktop", "1");
		p(provDoc, "AllowInternetSharing", "1");
		DOMUtils.createElement(provDoc, "UnapprovedInROMApplicationList");
		DOMUtils.createElement(provDoc, "ApprovedApplicationList");

	}
}
