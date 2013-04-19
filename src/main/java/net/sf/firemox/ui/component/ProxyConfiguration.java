/*
 * Firemox is a turn based strategy simulator Copyright (C) 2003-2007 Fabrice
 * Daugan
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import net.sf.firemox.tools.Configuration;
import net.sf.firemox.ui.ToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.mortbay.util.Password;

/**
 * The proxy configuration.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author hcross
 */
public class ProxyConfiguration extends JDialog implements ActionListener {

	/**
	 * Create a new instance of ProxyConfiguration dialog. All data are loaded.
	 */
	public ProxyConfiguration() {
		String passwordTxt = "";
		String loginTxt = "";
		if (clearLoginPwd.indexOf(':') != -1) {
			passwordTxt = clearLoginPwd.substring(clearLoginPwd.indexOf(':') + 1);
			loginTxt = clearLoginPwd.substring(0, clearLoginPwd.indexOf(':'));
		}

		proxyPasswordTxt = new JPasswordField(passwordTxt);
		proxyLoginTxt = new JTextField(loginTxt);

		setModal(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setTitle(LanguageManager.getString("wiz_proxy.title"));
		getContentPane().add(new JPanel(), BorderLayout.NORTH);
		getContentPane().add(new JPanel(), BorderLayout.EAST);
		getContentPane().add(new JPanel(), BorderLayout.WEST);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		// checkbox
		contentPanel.add(useProxy);

		// address
		JPanel proxyAddressPanel = new JPanel();
		proxyAddressPanel.setLayout(new BoxLayout(proxyAddressPanel,
				BoxLayout.X_AXIS));
		proxyAddressPanel.add(new JLabel(LanguageManager
				.getString("wiz_proxy.address")
				+ " : ", SwingConstants.RIGHT));
		proxyAddressTxt.setMaximumSize(new Dimension(2100, 20));
		proxyAddressPanel.add(proxyAddressTxt);
		contentPanel.add(proxyAddressPanel);

		// port
		JPanel proxyPortPanel = new JPanel();
		proxyPortPanel.setLayout(new BoxLayout(proxyPortPanel, BoxLayout.X_AXIS));
		proxyPortPanel.add(new JLabel(LanguageManager.getString("wiz_proxy.port")
				+ " : ", SwingConstants.RIGHT));
		proxyPortTxt.setMaximumSize(new Dimension(2100, 20));
		proxyPortPanel.add(proxyPortTxt);
		contentPanel.add(proxyPortPanel);

		// login
		JPanel proxyLoginPanel = new JPanel();
		proxyLoginPanel.setLayout(new BoxLayout(proxyLoginPanel, BoxLayout.X_AXIS));
		proxyLoginPanel.add(new JLabel(LanguageManager
				.getString("wiz_proxy.username")
				+ " : ", SwingConstants.RIGHT));
		proxyLoginTxt.setMaximumSize(new Dimension(2100, 20));
		proxyLoginPanel.add(proxyLoginTxt);
		contentPanel.add(proxyLoginPanel);

		// password
		JPanel proxyPasswordPanel = new JPanel();
		proxyPasswordPanel.setLayout(new BoxLayout(proxyPasswordPanel,
				BoxLayout.X_AXIS));
		proxyPasswordPanel.add(new JLabel(LanguageManager
				.getString("wiz_proxy.password")
				+ " : ", SwingConstants.RIGHT));
		proxyPasswordTxt.setMaximumSize(new Dimension(2100, 20));
		proxyPasswordPanel.add(proxyPasswordTxt);
		contentPanel.add(proxyPasswordPanel);

		getContentPane().add(contentPanel, BorderLayout.CENTER);

		// buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		pressme.setMnemonic('o'); // associate hotkey
		pressme.addActionListener(this); // register button
		pressme.setPreferredSize(new Dimension(50, 20));
		buttonPanel.add(pressme);
		cancel.setMnemonic('c'); // associate hotkey
		cancel.addActionListener(this); // register button
		cancel.setPreferredSize(new Dimension(50, 20));
		ToolKit.addCancelByEscapeKey(this, cancel);
		buttonPanel.add(cancel);
		setBounds(100, 100, 350, 200); // position frame
		pressme.requestFocus();
		getRootPane().setDefaultButton(pressme);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == pressme) {
			// save the settings
			Configuration.setProperty("proxyObfuscatedLoginPwd", Password
					.obfuscate(proxyLoginTxt.getText() + ':'
							+ new String(proxyPasswordTxt.getPassword())));
			Configuration.setProperty("proxyHost", proxyAddressTxt.getText());
			Configuration.setProperty("proxyPort", proxyPortTxt.getText().replace(
					",", "").replace(" ", ""));
			Configuration.setProperty("useProxy", this.useProxy.isSelected());
			dispose();
		} else if (source == cancel) {
			dispose();
		}
	}

	/**
	 * Ok button.
	 */
	private final JButton pressme = new JButton(LanguageManager.getString("ok"));

	/**
	 * Cancel button.
	 */
	private final JButton cancel = new JButton(LanguageManager
			.getString("cancel"));

	/**
	 * The string containing clear login:password pair.
	 */
	private final String clearLoginPwd = Password.deobfuscate(Configuration
			.getString("proxyObfuscatedLoginPwd", ""));

	/**
	 * The proxy host name/address/
	 */
	private final JTextField proxyAddressTxt = new JTextField(Configuration
			.getString("proxyHost", "192.168.0.252"));

	/**
	 * Proxy port.
	 */
	private final JTextField proxyPortTxt = new JTextField(Configuration
			.getString("proxyPort", "1299"));

	/**
	 * Password UI component.
	 */
	private JPasswordField proxyPasswordTxt;

	private JTextField proxyLoginTxt;

	/**
	 * Proxy checkBox activation.
	 */
	private JCheckBox useProxy = new JCheckBox(LanguageManager
			.getString("wiz_proxy.enable"), Configuration.getBoolean("useProxy",
			false));

}