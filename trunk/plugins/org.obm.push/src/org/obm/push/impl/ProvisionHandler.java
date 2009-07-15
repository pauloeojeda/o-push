package org.obm.push.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class ProvisionHandler implements IRequestHandler {

	private static final Log logger = LogFactory.getLog(ProvisionHandler.class);

	private IBackend backend;

	public ProvisionHandler(IBackend backend) {
		this.backend = backend;
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

		try {
			Document ret = DOMUtils.createDoc(null, "Provision");
			Element root = ret.getDocumentElement();
			DOMUtils.createElementAndText(root, "Status", "1");
			Element policies = DOMUtils.createElement(root, "Policies");
			Element policy = DOMUtils.createElement(policies, "Policy");
			DOMUtils.createElementAndText(policy, "PolicyType", policyType);
			DOMUtils.createElementAndText(policy, "Status", "1");
			if ("0".equals(pKey)) {
				pKey = "" + System.currentTimeMillis();
			}
			DOMUtils.createElementAndText(policy, "PolicyKey", pKey);
			Element data = DOMUtils.createElement(policy, "Data");

			// Exchange 2k7 response :
			// <DevicePasswordEnabled>0</DevicePasswordEnabled>
			// <AlphanumericDevicePasswordRequired>0</AlphanumericDevicePasswordRequired>
			// <RequireStorageCardEncryption>0</RequireStorageCardEncryption>
			// <DeviceEncryptionEnabled>0</DeviceEncryptionEnabled>
			// <DocumentBrowseEnabled>1</DocumentBrowseEnabled>
			// <AttachmentsEnabled>4</AttachmentsEnabled>
			// <MinDevicePasswordLength>900</MinDevicePasswordLength>
			// <MaxInactivityTimeDeviceLock>8</MaxInactivityTimeDeviceLock>
			// <MaxDevicePasswordFailedAttempts/>
			// <MaxAttachmentSize>1</MaxAttachmentSize>
			// <AllowSimpleDevicePassword/>
			// <DevicePasswordExpiration>0</DevicePasswordExpiration>
			// <DevicePasswordHistory>1</DevicePasswordHistory>
			// <AllowStorageCard>1</AllowStorageCard>
			// <AllowCamera>0</AllowCamera>
			// <RequireDeviceEncryption>1</RequireDeviceEncryption>
			// <AllowUnsignedApplications>1</AllowUnsignedApplications>
			// <AllowUnsignedInstallationPackages>3</AllowUnsignedInstallationPackages>
			// <MinDevicePasswordComplexCharacters>1</MinDevicePasswordComplexCharacters>
			// <AllowWiFi>1</AllowWiFi>
			// <AllowTextMessaging>1</AllowTextMessaging>
			// <AllowPOPIMAPEmail>2</AllowPOPIMAPEmail>
			// <AllowBluetooth>1</AllowBluetooth>
			// <AllowIrDA>0</AllowIrDA>
			// <RequireManualSyncWhenRoaming>1</RequireManualSyncWhenRoaming>
			// <AllowDesktopSync>0</AllowDesktopSync>
			// <MaxCalendarAgeFilter>1</MaxCalendarAgeFilter>
			// <AllowHTMLEmail>0</AllowHTMLEmail>
			// <MaxEmailAgeFilter>-1</MaxEmailAgeFilter>
			// <MaxEmailBodyTruncationSize>-1</MaxEmailBodyTruncationSize>
			// <MaxEmailHTMLBodyTruncationSize>0</MaxEmailHTMLBodyTruncationSize>
			// <RequireSignedSMIMEMessages>0</RequireSignedSMIMEMessages>
			// <RequireEncryptedSMIMEMessages>0</RequireEncryptedSMIMEMessages>
			// <RequireSignedSMIMEAlgorithm>0</RequireSignedSMIMEAlgorithm>
			// <RequireEncryptionSMIMEAlgorithm>2</RequireEncryptionSMIMEAlgorithm>
			// <AllowSMIMEEncryptionAlgorithmNegoti>1</AllowSMIMEEncryptionAlgorithmNegoti>
			// <ation>1</ation>
			// <AllowSMIMESoftCerts>1</AllowSMIMESoftCerts>
			// <AllowBrowser>1</AllowBrowser>
			// <AllowConsumerEmail>1</AllowConsumerEmail>
			// <AllowRemoteDesktop/>
			// <UnapprovedInROMApplicationList/>

			Element provDoc = DOMUtils.createElement(data, "EASProvisionDoc");

			Policy pol = backend.getDevicePolicy(bs);
			serializePolicy(provDoc, pol);

			// TODO
			// <eas-provisioningdoc>
			// <DevicePasswordEnabled>1</DevicePasswordEnabled>
			// <AlphanumericDevicePasswordRequired>1</AlphanumericDevicePasswordRequired>
			// <PasswordRecoveryEnabled>1</PasswordRecoveryEnabled>
			// <DeviceEncryptionEnabled>1</DeviceEncryptionEnabled>
			// <AttachmentsEnabled>1</AttachmentsEnabled>
			// <MinDevicePasswordLength/>
			// <MaxInactivityTimeDeviceLock>333</MaxInactivityTimeDeviceLock>
			// <MaxDevicePasswordFailedAttempts>8</MaxDevicePasswordFailedAttempts>
			// <MaxAttachmentSize/>
			// <AllowSimpleDevicePassword>0</AllowSimpleDevicePassword>
			// <DevicePasswordExpiration/>
			// <DevicePasswordHistory>0</DevicePasswordHistory>
			// </eas-provisioningdoc>

			responder.sendResponse("Provision", ret);

		} catch (Exception e) {
			logger.error("Error creating provision response");
		}

	}

	private void serializePolicy(Element provDoc, Policy p) {
		DOMUtils.createElementAndText(provDoc, "DevicePasswordEnabled", "0");
	}
}
