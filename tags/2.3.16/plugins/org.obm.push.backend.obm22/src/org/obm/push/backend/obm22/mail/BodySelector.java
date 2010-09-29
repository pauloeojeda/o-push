package org.obm.push.backend.obm22.mail;

import org.minig.imap.mime.MimePart;

public class BodySelector {

	private boolean pickupPlain;

	public BodySelector(boolean pickupPlain) {
		this.pickupPlain = pickupPlain;
	}

	public MimePart findBodyTextPart(MimePart root, boolean findForward) {
		boolean fetchPlain = false;
		MimePart chosenPart = null;
		for (MimePart mp : root) {
			if (mp.getMimeType() != null) {
				if (mp.getMimeType().equalsIgnoreCase("text")) {
					if (mp.getMimeSubtype().equalsIgnoreCase("html")
							&& !pickupPlain && (!inEml(mp) || findForward)
							&& !mp.getBodyParams().containsKey("name")) {
						chosenPart = mp;
						break;
					} else if (mp.getMimeSubtype().equalsIgnoreCase("plain")) {
						if (!fetchPlain) {
							chosenPart = mp;
							fetchPlain = true;
						}
						if (pickupPlain) {
							break;
						}
					}
				}
			} else {
				MimePart mpChild = mp.getChildren().get(0);
				if (mpChild.getMimeType() == null && chosenPart == null) {
					chosenPart = findBodyTextPart(mp, findForward);
				} else {
					if (!mpChild.getFullMimeType().equalsIgnoreCase(
							"message/rfc822")
							|| chosenPart == null) {
						chosenPart = findBodyTextPart(mp, findForward);
					}
				}
			}
		}
		return chosenPart;
	}

	private boolean inEml(MimePart mp) {
		return mp.getParent() != null
				&& "rfc822".equalsIgnoreCase(mp.getParent().getMimeSubtype());
	}

}
