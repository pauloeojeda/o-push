package org.obm.push.backend.obm22.mail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.MSEmailBodyType;

import fr.aliasource.utils.FileUtils;


public class ReplyEmailHandler extends SendEmailHandler{

	private MSEmail originMail;
	
	public ReplyEmailHandler(String defaultFrom, MSEmail originMail) {
		super(defaultFrom);
		this.originMail = originMail;

	}
	
	@Override
	public void body(BodyDescriptor arg0, InputStream arg1)
			throws MimeException, IOException {
		MimeHeader mimeHeader = new MimeHeader(header);
		root = new LocalMimePart(mimeHeader);
		String body = FileUtils.streamString(arg1, false);
		
		String oldBody = this.originMail.getBody().getValue(MSEmailBodyType.PlainText);
		if(oldBody != null){
			body += oldBody;
		}
		
		CharSequenceSource css = new CharSequenceSource(body.trim());
		root.setBody(css);
	}
}
