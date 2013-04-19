/*
 * Created on Jul 21, 2004 
 * 
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 */
package net.sf.firemox.mail;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class MimeMessage2 extends MimeMessage {

	/**
	 * Creates a new instance of MimeMessage2 <br>
	 * 
	 * @param session
	 *          the session.
	 */
	public MimeMessage2(Session session) {
		super(session);
	}

	@Override
	public void saveChanges() throws MessagingException {
		super.saveChanges();
		setMessageId();
	}

	/**
	 * Redefine the <code>updateHeaders()</code> function in order to customize
	 * the "Message-ID" header using the "mail.smtp.host" property to define it
	 * instead of the <code>uniqueId.JavaMail@host</code>string.
	 * 
	 * @throws MessagingException
	 *           building mail error.
	 */
	@Override
	protected void updateHeaders() throws MessagingException {
		super.updateHeaders();
		setMessageId();
	}

	private void setMessageId() {
		String nativID;
		try {
			nativID = super.getMessageID();
			final String javaMail = "JavaMail";
			int ind = nativID.indexOf(javaMail);
			if (ind != -1) {
				String messageId = nativID.substring(0, ind)
						+ nativID.substring(ind + javaMail.length() + 1, nativID
								.indexOf('@') + 1) + System.getProperty("mail.smtp.host") + ">";
				super.headers.setHeader("Message-Id", messageId);
				super.setHeader("Message-Id", messageId);
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}
