package org.obm.push.backend.obm22.mail;

import java.io.ByteArrayOutputStream;
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
import org.columba.ristretto.composer.MimeTreeRenderer;
import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;
import org.columba.ristretto.message.LocalMimePart;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimeType;
import org.columba.ristretto.parser.AddressParser;
import org.columba.ristretto.parser.ParserException;

import fr.aliasource.utils.FileUtils;

public class SendEmailHandler implements
		org.apache.james.mime4j.parser.ContentHandler {

	protected Log logger = LogFactory.getLog(getClass());
	protected Header header;
	private BasicHeader basicHeader;
	protected LocalMimePart root;
	private MimeHeader rootMimeHeader;

	protected LocalMimePart current;
	
	private Boolean isMultiPart;
	private Boolean inBody;

	private LocalMimePart localMimePart;
	private MimeHeader localMimeHeader;

	private Set<Address> to;
	private String from;

	public SendEmailHandler(String defaultFrom) {
		this.to = new HashSet<Address>();
		this.from = defaultFrom;
		header = new Header();
		basicHeader = new BasicHeader(header);
		rootMimeHeader = new MimeHeader(header);
		root = new LocalMimePart(rootMimeHeader);
		current = root;

		isMultiPart = false;
		inBody = false;
	}

	public Set<Address> getTo() {
		return to;
	}

	public String getFrom() {
		return from;
	}

	public String getMessage() {
		InputStream in = null;
		try {
			in = MimeTreeRenderer.getInstance().renderMimePart(root);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			FileUtils.transfer(in, out, true);
			byte[] data = out.toByteArray();
			return new String(data);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	@Override
	public void startHeader() throws MimeException {
	}

	@Override
	public void endHeader() throws MimeException {
	}

	@Override
	public void endMultipart() throws MimeException {
	}

	@Override
	public void field(Field arg0) throws MimeException {
		if (!inBody) {
			if ("to".equalsIgnoreCase(arg0.getName())) {
				try {
					Address[] adds = AddressParser.parseMailboxList(arg0
							.getBody());
					for (Address add : adds) {
						this.to.add(add);
					}
				} catch (ParserException e) {
					throw new MimeException(e.getMessage());
				}
				basicHeader.set(arg0.getName(), arg0.getBody());
			} else if ("from".equalsIgnoreCase(arg0.getName())) {
				if (arg0.getBody() == null || !"".equals(arg0.getBody())) {
					if (this.from != null && from.contains("@")) {
						String[] tab = from.split("@");
						Mailbox mb = new Mailbox(tab[0], tab[1]);
						arg0 = Fields.from(mb);
					}
				}
				this.from = arg0.getBody();
				basicHeader.set(arg0.getName(), arg0.getBody());
			} else if ("Content-Type".equalsIgnoreCase(arg0.getName())) {
				if (arg0.getBody().toLowerCase().contains("multipart")) {
					MimeType mt = parseMultiPart(arg0.getBody());
					if(mt != null){
						rootMimeHeader.setMimeType(mt);	
					}
//					this.current = root;
					this.isMultiPart = true;
				}
			} else {
				basicHeader.set(arg0.getName(), arg0.getBody());
			}
		} else {
			if ("Content-Type".equalsIgnoreCase(arg0.getName())) {
				if (arg0.getBody().toLowerCase().contains("multipart")) {
					MimeType mt = parseMultiPart(arg0.getBody());
					if(mt != null){
						localMimeHeader.setMimeType(mt);	
					}
					this.current = localMimePart;
					this.isMultiPart = true;
				} else {
					localMimeHeader.set(arg0.getName(), arg0.getBody());
				}
			} else {
				localMimeHeader.set(arg0.getName(), arg0.getBody());
			}
			
		}
	}

	private MimeType parseMultiPart(String body) {
		String[] tab = body.split(";");
		for (String part : tab) {
			if (part.toLowerCase().contains("multipart")) {
				int i = part.indexOf("/");
				if (i > 0) {
					String type = part.substring(0, i);
					String subType = part.substring(i + 1);
					return new MimeType(type, subType);
				}
			}
		}
		return null;
	}

	@Override
	public void startMultipart(BodyDescriptor arg0) throws MimeException {
	}

	@Override
	public void body(BodyDescriptor arg0, InputStream arg1)
			throws MimeException, IOException {
		CharSequenceSource css = new CharSequenceSource(FileUtils.streamString(
				arg1, false));
		if(localMimePart == null){
			localMimePart = root;
		}
		localMimePart.setBody(css);
	}

	@Override
	public void startBodyPart() throws MimeException {
		this.inBody = true;
		localMimeHeader = new MimeHeader();
		if (!isMultiPart) {
			localMimePart = current;
		} else {
			localMimePart = new LocalMimePart(localMimeHeader);
			current.addChild(this.localMimePart);
		}
	}

	@Override
	public void endBodyPart() throws MimeException {
	}

	@Override
	public void endMessage() throws MimeException {
	}

	@Override
	public void epilogue(InputStream arg0) throws MimeException, IOException {
	}

	@Override
	public void preamble(InputStream arg0) throws MimeException, IOException {
	}

	@Override
	public void raw(InputStream arg0) throws MimeException, IOException {
	}

	@Override
	public void startMessage() throws MimeException {
	}
}
