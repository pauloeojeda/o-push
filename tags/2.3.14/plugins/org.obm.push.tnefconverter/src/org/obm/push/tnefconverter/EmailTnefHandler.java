package org.obm.push.tnefconverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import net.freeutils.tnef.Message;
import net.freeutils.tnef.TNEFInputStream;
import net.freeutils.tnef.TNEFUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.descriptor.BodyDescriptor;
import org.apache.james.mime4j.parser.Field;
import org.columba.ristretto.coder.EncodedWord;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.AddressParser;
import org.columba.ristretto.parser.ParserException;
import org.obm.push.utils.Base64;
import org.obm.push.utils.FileUtils;

public class EmailTnefHandler implements
		org.apache.james.mime4j.parser.ContentHandler {

	protected Log logger = LogFactory.getLog(getClass());

	private Set<Address> to;
	private Set<Address> cc;
	private Address from;
	private String subject;

	private Message tnefMsg;
	private InputStream tnefDoc;

	private String threadTopic;

	public EmailTnefHandler() {
		this.to = new HashSet<Address>();
		this.cc = new HashSet<Address>();
	}

	public Address getFrom() {
		return from;
	}

	public Set<Address> getTo() {
		return to;
	}

	public Set<Address> getCc() {
		return cc;
	}

	public String getSubject() {
		return threadTopic != null && threadTopic.length()>0 ? threadTopic : subject;
	}

	public Message getTNEFMsg() {
		return tnefMsg;
	}

	public InputStream getTnefDoc() {
		return tnefDoc;
	}

	@Override
	public void field(Field arg0) throws MimeException {
		if ("subject".equalsIgnoreCase(arg0.getName())) {
			this.subject = EncodedWord.decode(arg0.getBody()).toString();
		} else if ("thread-topic".equalsIgnoreCase(arg0.getName())) {
			this.threadTopic = EncodedWord.decode(arg0.getBody()).toString();
		} else if ("to".equalsIgnoreCase(arg0.getName())) {
			try {
				Address[] adds = AddressParser.parseMailboxList(arg0.getBody());
				for (Address add : adds) {
					this.to.add(add);
				}
			} catch (ParserException e) {
				throw new MimeException(e.getMessage());
			}
		} else if ("cc".equalsIgnoreCase(arg0.getName())) {
			try {
				Address[] adds = AddressParser.parseMailboxList(arg0.getBody());
				for (Address add : adds) {
					this.cc.add(add);
				}
			} catch (ParserException e) {
				throw new MimeException(e.getMessage());
			}
		} else if ("from".equalsIgnoreCase(arg0.getName())) {
			try {
				this.from = AddressParser.parseAddress(arg0.getBody());
			} catch (ParserException e) {
				throw new MimeException(e.getMessage());
			}
		}
	}

	@Override
	public void body(BodyDescriptor arg0, InputStream arg1)
			throws MimeException, IOException {
		if (TNEFUtils.isTNEFMimeType(arg0.getMimeType())) {
			byte[] bb = FileUtils.streamBytes(arg1, false);
			this.tnefDoc = new ByteArrayInputStream(bb);
			try {
				if ("base64".equalsIgnoreCase(arg0.getTransferEncoding())) {
					bb = Base64.decode(new String(bb).toCharArray());
					arg1 = new ByteArrayInputStream(bb);
				}
				TNEFInputStream tnef = new TNEFInputStream(arg1);
				this.tnefMsg = new Message(tnef);
			} catch (Exception e) {
				storeTnef(bb);
				throw new MimeException(e);
			}
		}
	}

	private void storeTnef(byte[] b) {
		try {
			if (b != null) {
				File tmp = File.createTempFile("debug_", ".tnef");
				FileOutputStream fout = new FileOutputStream(tmp);
				fout.write(b);
				fout.close();
				logger.error("unparsable tnef saved in "
						+ tmp.getAbsolutePath());
			}
		} catch (Throwable t) {
			logger.error("error storing debug file", t);
		}
	}

	@Override
	public void startMultipart(BodyDescriptor arg0) throws MimeException {
	}

	@Override
	public void endMultipart() throws MimeException {
	}

	@Override
	public void startBodyPart() throws MimeException {
	}

	@Override
	public void endBodyPart() throws MimeException {
	}

	@Override
	public void startHeader() throws MimeException {
	}

	@Override
	public void endHeader() throws MimeException {
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
