package org.obm.push.backend.obm22.mail;

import org.obm.push.backend.MSEmail;
import org.obm.push.backend.MSEmailBodyType;


public class ReplyEmailHandler extends SendEmailHandler{

	private MSEmail originMail;
	
	public ReplyEmailHandler(String defaultFrom, MSEmail originMail) {
		super(defaultFrom);
		this.originMail = originMail;

	}
	
	@Override
	public String getMessage() {
		String mm = super.getMessage().trim();
		try {
			String oldBody = this.originMail.getBody().getValue(MSEmailBodyType.PlainText);
			if(oldBody != null){
				mm += oldBody;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
 
		return mm.trim();
	}
}
