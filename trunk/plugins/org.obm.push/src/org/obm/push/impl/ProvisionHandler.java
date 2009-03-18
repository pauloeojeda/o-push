package org.obm.push.impl;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

	private static final Log logger = LogFactory
			.getLog(ProvisionHandler.class);

	private IBackend backend;

	public ProvisionHandler(IBackend backend) {
		this.backend = backend;
	}

	@Override
	public void process(ASParams p, Document doc, Responder responder) {
		logger.info("process(" + p.getUserId() + "/" + p.getDevType() + ")");

		String policyType = DOMUtils.getUniqueElement(doc.getDocumentElement(), "PolicyType").getTextContent();
		
		try {
			Document ret = DOMUtils.createDoc(null, "Provision");
			Element root = ret.getDocumentElement();
			DOMUtils.createElementAndText(root, "Status", "1");
			Element policies = DOMUtils.createElement(root, "Policies");
			Element policy = DOMUtils.createElement(policies, "Policy");
			DOMUtils.createElementAndText(policy, "PolicyType", policyType);
			DOMUtils.createElementAndText(policy, "Status", "1");
			DOMUtils.createElementAndText(policy, "PolicyKey", UUID.randomUUID().toString());
			Element data = DOMUtils.createElement(policy, "Data");
			
			Element provDoc = DOMUtils.createElement(data, "eas-provisioningdoc");
			
			Policy pol = backend.getDevicePolicy();
			if (p != null) {
				serializePolicy(provDoc, pol);
			}
			
			
			// TODO
//            <eas-provisioningdoc>
//            	<DevicePasswordEnabled>1</DevicePasswordEnabled>
//            	<AlphanumericDevicePasswordRequired>1</AlphanumericDevicePasswordRequired>
//            	<PasswordRecoveryEnabled>1</PasswordRecoveryEnabled>
//            	<DeviceEncryptionEnabled>1</DeviceEncryptionEnabled>
//            	<AttachmentsEnabled>1</AttachmentsEnabled>
//            	<MinDevicePasswordLength/>
//            	<MaxInactivityTimeDeviceLock>333</MaxInactivityTimeDeviceLock>
//            	<MaxDevicePasswordFailedAttempts>8</MaxDevicePasswordFailedAttempts>
//            	<MaxAttachmentSize/>
//            	<AllowSimpleDevicePassword>0</AllowSimpleDevicePassword>
//            	<DevicePasswordExpiration/>
//            	<DevicePasswordHistory>0</DevicePasswordHistory>
//            </eas-provisioningdoc>

			responder.sendResponse("Provision", ret);
			
		} catch (Exception e) {
			logger.error("Error creating provision response");
		}
		
		
		
	}

	private void serializePolicy(Element provDoc, Policy p) {
		// TODO Auto-generated method stub
		
	}
}
