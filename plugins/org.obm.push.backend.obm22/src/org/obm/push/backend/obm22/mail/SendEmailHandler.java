package org.obm.push.backend.obm22.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.field.Fields;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.parser.Field;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.AddressParser;
import org.columba.ristretto.parser.ParserException;

import fr.aliasource.utils.FileUtils;

public class SendEmailHandler implements
		org.apache.james.mime4j.parser.ContentHandler {

	protected Log logger = LogFactory.getLog(getClass());
	
	private StringBuilder message;
	private Boolean inHeader;
	private Boolean inMultipart;

	private Set<Address> to;
	private String from;

	public SendEmailHandler(String defaultFrom) {
		inHeader = false;
		inMultipart = false;
		this.to = new HashSet<Address>();
		this.from = defaultFrom;
		this.message = new StringBuilder();
	}

	public Set<Address> getTo() {
		return to;
	}

	public String getFrom() {
		return from;
	}

	public String getMessage() {
		return message.toString();
	}

	@Override
	public void endHeader() throws MimeException {
		if (!inMultipart) {
			inHeader = false;
		}
		this.message.append("\r\n");
	}

	@Override
	public void endMultipart() throws MimeException {
		this.inMultipart = false;

	}

	@Override
	public void field(Field arg0) throws MimeException {
		if (inHeader) {
			if ("to".equalsIgnoreCase(arg0.getName())) {
				try {
					Address[] adds = AddressParser.parseMailboxList(arg0.getBody());
					for(Address add : adds){
						this.to.add(add);
					}
				} catch (ParserException e) {
					throw new MimeException(e.getMessage());
				}
			} else if ("from".equalsIgnoreCase(arg0.getName())) {
				if (arg0.getBody() == null || !"".equals(arg0.getBody())) {
					if (this.from != null && from.contains("@")) {
						String[] tab = from.split("@");
						Mailbox mb = new Mailbox(tab[0], tab[1]);
						arg0 = Fields.from(mb);
					}
				}
				this.from = arg0.getBody();
			}
		}
		appendToMessage(arg0.getRaw().toString());
	}

	@Override
	public void startHeader() throws MimeException {
		if (!inMultipart) {
			inHeader = true;
		}
	}

	@Override
	public void startMultipart(BodyDescriptor arg0) throws MimeException {
		this.inMultipart = true;
	}

	@Override
	public void body(BodyDescriptor arg0, InputStream arg1)
			throws MimeException, IOException {
		appendToMessage(FileUtils.streamString(arg1, false));
	}

	@Override
	public void endBodyPart() throws MimeException {
	}

	@Override
	public void endMessage() throws MimeException {
	}

	@Override
	public void epilogue(InputStream arg0) throws MimeException, IOException {
		appendToMessage(FileUtils.streamString(arg0, false));
	}

	@Override
	public void preamble(InputStream arg0) throws MimeException, IOException {
		appendToMessage(FileUtils.streamString(arg0, false));
	}

	@Override
	public void raw(InputStream arg0) throws MimeException, IOException {
		appendToMessage(FileUtils.streamString(arg0, false));
	}

	@Override
	public void startBodyPart() throws MimeException {
	}

	@Override
	public void startMessage() throws MimeException {
	}

	protected void appendToMessage(String ligne) {
		this.message.append(ligne);
		this.message.append("\r\n");
	}
}
