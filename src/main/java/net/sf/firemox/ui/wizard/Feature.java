/*
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
 */
package net.sf.firemox.ui.wizard;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import net.sf.firemox.mail.MailUtils;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.component.JLink;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class Feature extends YesNo {

	/**
	 * Creates a new instance of Bug <br>
	 */
	public Feature() {
		super(LanguageManager.getString("wiz_featurerequest.title"),
				LanguageManager.getString("wiz_featurerequest.description"),
				"wiz_featurerequest.png", LanguageManager.getString("send"),
				LanguageManager.getString("cancel"), 600, 400);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		gameParamPanel.setLayout(new BoxLayout(gameParamPanel, BoxLayout.Y_AXIS));

		JPanel userPanel = new JPanel();
		userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.X_AXIS));
		userPanel.add(new JLabel(LanguageManager
				.getString("wiz_sf.sourceforgename")
				+ " : "));
		userNameTxt = new JTextField(Configuration.getString("sourceforgeName", ""));
		userPanel.add(userNameTxt);
		userPanel.setMaximumSize(new Dimension(2000, 22));
		JLink sfLabel = new JLink(
				"http://sourceforge.net/tracker/?func=add&group_id="
						+ IdConst.PROJECT_ID + "&atid=601043", UIHelper
						.getIcon("sourceforge_logo.png"));
		userPanel.add(sfLabel);
		gameParamPanel.add(userPanel);

		JPanel summaryPanel = new JPanel();
		summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.X_AXIS));
		summaryPanel.add(new JLabel(LanguageManager.getString("wiz_sf.summary")
				+ " : "));
		subjectTxt = new JTextField();
		summaryPanel.add(subjectTxt);
		summaryPanel.setMaximumSize(new Dimension(2000, 22));
		gameParamPanel.add(summaryPanel);

		messageTxt = new JTextArea();
		JScrollPane scroll = new JScrollPane(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setAutoscrolls(true);
		scroll.setViewportView(messageTxt);
		gameParamPanel.add(scroll);

		getRootPane().setDefaultButton(null);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == okBtn) {
			/*
			 * invoked when user want to report a bug
			 */
			Configuration.setProperty("sourceforgeName", userNameTxt.getText());
			MailUtils.sendEmail("", "feature_request_form@" + IdConst.PROJECT_NAME
					+ ".net", new String[] { "feature_request_jfiremox@yahoo.fr" },
					"Username:" + userNameTxt.getText() + "\nSummary:"
							+ subjectTxt.getText() + "\n\n" + messageTxt.getText(), "["
							+ IdConst.PROJECT_NAME + "] " + subjectTxt.getText(), null, null,
					"feature_request", "mx4.mail.yahoo.com"); // "smtp.mail.yahoo.fr");
		}
		super.actionPerformed(event);
	}

	/**
	 * Text field containing sourceforge username
	 */
	private JTextField userNameTxt;

	/**
	 * Text field containing message
	 */
	private JTextArea messageTxt;

	/**
	 * Text field containing the summary of this report
	 */
	private JTextField subjectTxt;
}