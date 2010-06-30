package org.obm.push.backend;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MSAddress {
	private String mail;
	private String displayName;

	private static final Log logger = LogFactory.getLog(MSAddress.class);

	public MSAddress(String mail) {
		this(null, mail);
	}

	public MSAddress(String displayName, String mail) {
		if (displayName != null) {
			this.displayName = displayName.replace("\"", "").replace("<", "")
					.replace(">", "");
		}
		if (mail != null && mail.contains("@")) {
			this.mail = mail.replace("\"", "").replace("<", "")
					.replace(">", "");
		} else {
			// FIXME ...
			if (logger.isDebugEnabled()) {
				logger
						.debug("mail: "
								+ mail
								+ " is not a valid email, building a john.doe@minig.org");
			}
			this.displayName = mail.replace("\"", "").replace("<", "").replace(
					">", "");
			this.mail = "john.doe@minig.org";
		}
	}

	public String getMail() {
		return mail;
	}

	public String getDisplayName() {
		return displayName;
	}
}
