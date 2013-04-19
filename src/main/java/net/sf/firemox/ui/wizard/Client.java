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
 * 
 */
package net.sf.firemox.ui.wizard;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import net.sf.firemox.deckbuilder.DeckConstraint;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.component.LoaderConsole;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.mortbay.util.Password;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
public class Client extends Network {

	/**
	 * Creates a new instance of Client <br>
	 */
	public Client() {
		super(LanguageManager.getString("wiz_client.title"), LanguageManager
				.getString("wiz_client.description"), LanguageManager
				.getString("wiz_client.ok"), 460, 270);
		try {
			passwordTxt.setText(Password.deobfuscate(Configuration.getString(
					"clientpswd", "")));
		} catch (StringIndexOutOfBoundsException e) {
			Log.error("Error while trying to deobfuscate password");
		}
		portTxt.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_network.port.tooltip"));
		ipTxt.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_client.ip.tooltip"));
		addCheckValidity(ipTxt);
		checkValidity();
	}

	@Override
	protected boolean checkValidity() {
		if (!super.checkValidity()) {
			return false;
		}
		if (ipTxt.getText().length() == 0) {
			wizardInfo.resetError(LanguageManager.getString("wiz_client.ip.missed"));
			return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == okBtn) {
			/*
			 * invoked when user wanna join a game
			 */
			try {
				// verify validity of port
				Object value = portTxt.getValue();
				int port = Integer.parseInt(value == null ? portTxt.getText() : portTxt
						.getValue().toString());
				if (port < MINIMAL_PORT || port > 65535) {
					// out of range integer{
					JOptionPane.showMessageDialog(this, LanguageManager
							.getString("wiz_network.port.invalid"), LanguageManager
							.getString("error"), JOptionPane.WARNING_MESSAGE);
					return;
				}
				if (!validateDeck()) {
					return;
				}

				setVisible(false);

				// all parameters seem to be valid, save them
				Configuration.setProperty("port", port);
				Configuration.setProperty("decks.constraint",
						((DeckConstraint) constraintList.getSelectedItem()).getName());
				ConnectionManager.closeConnexions();
				Configuration.setProperty("clientPassword", Password
						.obfuscate(new String(passwordTxt.getPassword())));
				Configuration.setProperty("ip", ipTxt.getText());

				// Show the game launcher
				LoaderConsole.launchLoader();
				ConnectionManager.client = new net.sf.firemox.network.Client(deck,
						passwordTxt.getPassword());
				ConnectionManager.client.start();
			} catch (Exception e1) {
				ConnectionManager.closeConnexions();
				JOptionPane.showMessageDialog(this, LanguageManager
						.getString("wiz_network.unknownpb")
						+ " : " + e1.getMessage(), LanguageManager
						.getString("wiz_network.unknownpb"), JOptionPane.WARNING_MESSAGE);
				dispose();
				return;
			}
			setVisible(false);
			dispose();
		} else {
			super.actionPerformed(event);
		}
	}
}
