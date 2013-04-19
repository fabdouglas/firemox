/*
 * Created on Dec 15, 2004 
 * Original filename was Account.java
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
package net.sf.firemox.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import net.sf.firemox.tools.Configuration;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.ToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
public class Account extends JDialog implements ActionListener {

	/**
	 * Creates a new instance of Account <br>
	 * 
	 * @param accountName
	 */
	public Account(String accountName) {
		super();
		this.accountName = accountName;
		setModal(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setTitle(LanguageManager.getString("accountoptions") + " : "
				+ LanguageManager.getString(accountName));
		getContentPane().add(new JPanel(), BorderLayout.NORTH);
		getContentPane().add(new JPanel(), BorderLayout.EAST);
		getContentPane().add(new JPanel(), BorderLayout.WEST);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		// account
		JPanel accountPanel = new JPanel();
		accountPanel.setLayout(new BoxLayout(accountPanel, BoxLayout.X_AXIS));
		accountPanel.add(new JLabel(LanguageManager.getString(accountName) + " : ",
				SwingConstants.RIGHT));

		accountTxt = new JTextField(Configuration.getString(accountName));
		accountTxt.setMaximumSize(new Dimension(2100, 20));
		accountPanel.add(accountTxt);
		contentPanel.add(accountPanel);

		// checkbox
		Boolean value = Configuration.getBoolean(accountName + "public", null);
		if (value != null) {
			// this account's visiblity may be changed
			isPublic = new JCheckBox(LanguageManager.getString("ispublic"), value);
			contentPanel.add(isPublic);
		}

		getContentPane().add(contentPanel, BorderLayout.CENTER);

		// buttons
		JPanel buttonPanel = new JPanel();
		// buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		pressme.setMnemonic('o'); // associate hotkey
		pressme.addActionListener(this); // register button
		pressme.setMaximumSize(new Dimension(100, 26));
		buttonPanel.add(pressme);
		cancel.setMnemonic('c'); // associate hotkey
		cancel.addActionListener(this); // register button
		cancel.setMaximumSize(new Dimension(100, 26));
		ToolKit.addCancelByEscapeKey(this, cancel);
		buttonPanel.add(cancel);
		int height = 105 + (isPublic == null ? 0 : 20);
		setBounds((MagicUIComponents.magicForm.getWidth() - 250) / 2,
				(MagicUIComponents.magicForm.getHeight() - height) / 2, 250, height);
		pressme.requestFocus();
		getRootPane().setDefaultButton(pressme);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	}

	/**
	 * triggers the ok button
	 * 
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == pressme) {
			// save the settings
			Configuration.setProperty(accountName, accountTxt.getText());
			if (isPublic != null) {
				Configuration
						.setProperty(accountName + "public", isPublic.isSelected());
			}
			dispose();
		} else if (source == cancel) {
			dispose();
		}
	}

	private JButton pressme = new JButton(LanguageManager.getString("ok"));

	private JButton cancel = new JButton(LanguageManager.getString("cancel"));

	/**
	 * Account value
	 */
	private JTextField accountTxt;

	/**
	 * "IS Public" checkBox
	 */
	private JCheckBox isPublic;

	/**
	 * The account name.
	 */
	private String accountName;
}
