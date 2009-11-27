package org.obm.push.backend.obm22.mail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;
import org.columba.ristretto.message.MimeType;

import fr.aliasource.utils.FileUtils;

public class ForwardEmailHandler extends SendEmailHandler {

	private InputStream originMail;

	public ForwardEmailHandler(String defaultFrom, InputStream originMail) {
		super(defaultFrom);
		this.originMail = originMail;

	}

	@Override
	public void body(BodyDescriptor arg0, InputStream arg1)
			throws MimeException, IOException {
		MimeHeader mimeHeader = new MimeHeader(header);
		mimeHeader.setMimeType(new MimeType("multipart", "mixed"));
		root = new LocalMimePart(mimeHeader);

		LocalMimePart textPart = new LocalMimePart(new MimeHeader());
		CharSequenceSource css = new CharSequenceSource(FileUtils.streamString(
				arg1, false));
		textPart.setBody(css);
		root.addChild(textPart);

		MimePart attachmentPart = prepareAttachement();
		root.addChild(attachmentPart);
	}

	private MimePart prepareAttachement()
			throws IOException, FileNotFoundException {
		String filename = "forwarded_message.eml";
		MimeHeader attachmentHeader = new MimeHeader("message", "rfc822");

		attachmentHeader.putContentParameter("name", filename);
		attachmentHeader.setContentDisposition("attachment");
		attachmentHeader.putDispositionParameter("filename", filename);

		LocalMimePart attachmentPart = new LocalMimePart(attachmentHeader);
		CharSequenceSource css = new CharSequenceSource(FileUtils.streamString(
				originMail, false));
		attachmentPart.setBody(css);

		return attachmentPart;
	}
}
