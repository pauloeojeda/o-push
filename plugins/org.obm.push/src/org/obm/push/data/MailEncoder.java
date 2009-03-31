package org.obm.push.data;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IApplicationData;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class MailEncoder implements IDataEncoder {

//	<To>"Administrator" &lt;Administrator@buffy.kvm&gt;</To>
//    <From>"Administrator" &lt;Administrator@buffy.kvm&gt;</From>
//    <Subject>mail to me</Subject>
//    <DateReceived>2009-03-17T17:59:00.000Z</DateReceived>
//    <DisplayTo>Administrator</DisplayTo>
//    <ThreadTopic>mail to me</ThreadTopic>
//    <Importance>1</Importance>
//    <Read>1</Read>
//    <BodyTruncated>0</BodyTruncated>
//    <Body>to me from otlk&#13;</Body>
//    <MessageClass>IPM.Note</MessageClass>
//    <InternetCPID>20127</InternetCPID>
	
	@Override
	public void encode(BackendSession bs, Element parent, IApplicationData data) {
		// TODO Auto-generated method stub
		DOMUtils.createElementAndText(parent, "Email:To", "thomas@zz.com");
		DOMUtils.createElementAndText(parent, "Email:From", "root@buffy.kvm");
		DOMUtils.createElementAndText(parent, "Email:Subject", "mail from o-push");
		DOMUtils.createElementAndText(parent, "Email:DateReceived", "2009-03-17T17:59:00.000Z");
		DOMUtils.createElementAndText(parent, "Email:DisplayTo", "Thomas Cataldo");
		DOMUtils.createElementAndText(parent, "Email:ThreadTopic", "mail from o-push");
		DOMUtils.createElementAndText(parent, "Email:Importance", "1");
		DOMUtils.createElementAndText(parent, "Email:Read", "1");
		DOMUtils.createElementAndText(parent, "Email:BodyTruncated", "0");
		DOMUtils.createElementAndText(parent, "Email:Body", "from o-push server :-)");
		DOMUtils.createElementAndText(parent, "Email:MessageClass", "IPM.Note");
		DOMUtils.createElementAndText(parent, "Email:InternetCPID", "1234");
		
	}

}
