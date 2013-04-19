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

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.MToolKit;

/**
 * To send a mail.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class MailUtils {

	/**
	 * Create a new instance of this class.
	 */
	private MailUtils() {
		super();
	}

	/**
	 * 
	 */
	public static final String DEFAULT_CONTENT_TYPE = "text/html";

	/**
	 * @param userName
	 * @param from
	 * @param to
	 * @param message
	 * @param subject
	 * @param attachments
	 * @param headers
	 * @param mailerUser
	 * @param hostName
	 * @return a MIME message
	 */
	public static MimeMessage sendEmail(String userName, String from,
			String[] to, String message, String subject, List<String> attachments,
			Header[] headers, String mailerUser, String hostName) {

		final Properties props = System.getProperties();
		props.put("user.name", userName);
		props.put("mail.user", userName);
		// props.put("user.passwd", "secret");
		// props.put("user.psswd", "secret");
		// props.put("user.pwd", "secret");
		// props.put("mail.smtp.password", "secret");
		// props.put("mail.smtp.auth", "true");
		// props.put("mail.smtp.port", "25");
		props.put("mail.smtp.user", userName);
		props.put("mail.smtp.host", hostName);
		props.put("mail.host", hostName);
		props.put("mail.smtp.auth", "true");
		props.put("mail.transport.protocol", "smtp");
		props.putAll(MToolKit.getSmtpProperties());
		Session mailSession = null;

		if (Configuration.getBoolean("useProxy", false)) {
			mailSession = Session.getDefaultInstance(props, new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(props
							.getProperty("socks.proxyUserName"), props
							.getProperty("socks.proxyPassword"));
				}
			});
		} else {
			mailSession = Session.getDefaultInstance(props, null);
		}

		mailSession.setDebug(false);
		try {

			// load the key store
			MimeMessage msg = new MimeMessage2(mailSession);
			msg.setHeader("X-Mailer", "JavaMailer");
			MimeMultipart mp = new MimeMultipart();
			MimeBodyPart bodyPart = null;
			MimeBodyPart bodyPartTxt = new MimeBodyPart();

			// add the message text to the mail
			if (message != null) {
				bodyPartTxt.setContent(message, DEFAULT_CONTENT_TYPE);
				mp.addBodyPart(bodyPartTxt);
			}

			// add the attachment files
			if (attachments != null) {
				for (String attachment : attachments) {
					bodyPart = new MimeBodyPart();
					if (!new File(attachment).exists()) {
						throw new InternalError("File " + attachment + " does not exist");
					}
					FileDataSource fds = new FileDataSource(attachment);
					DataHandler dh = new DataHandler(fds);
					bodyPart.setFileName(attachment
							.substring(attachment.lastIndexOf('/') + 1));
					bodyPart.setDisposition(Part.ATTACHMENT);
					bodyPart.setDescription("File Attachment");
					bodyPart.setDataHandler(dh);
					mp.addBodyPart(bodyPart);
				}
			}

			// add the Recipients to
			InternetAddress[] addresses = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				// to[i]="f-daugan@laptop2.fabdouglas.fr";
				addresses[i] = new InternetAddress(to[i].trim());
			}
			msg.setRecipients(Message.RecipientType.TO, addresses);

			// add custom headers
			if (headers != null) {
				for (Header header : headers) {
					msg.addHeader(header.getHeaderName(), header.getHeaderValue());
				}
			}

			msg.setFrom(new InternetAddress(from));

			if (attachments == null || bodyPart == null) {
				// simple mail
				msg.setContent(bodyPartTxt.getContent(), bodyPartTxt.getContentType());
			} else {
				msg.setContent(mp);
			}

			msg.setSubject(subject);
			msg.setSentDate(new java.util.Date());
			msg.saveChanges();

			Transport transport = mailSession.getTransport("smtp");
			transport.connect(hostName, 25, userName, "");
			transport.sendMessage(msg, addresses);
			transport.close();
			return msg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}