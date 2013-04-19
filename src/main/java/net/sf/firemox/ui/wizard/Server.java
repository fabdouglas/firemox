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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.InetAddress;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.firemox.deckbuilder.DeckConstraint;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.StartingOption;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.component.LoaderConsole;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.mortbay.util.Password;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
public class Server extends Network {

	/**
	 * Creates a new instance of Client <br>
	 */
	public Server() {
		super(LanguageManager.getString("wiz_server.title"), LanguageManager
				.getString("wiz_server.description"), LanguageManager
				.getString("wiz_server.ok"), 460, 380);

		try {
			passwordTxt.setText(Password.deobfuscate(Configuration.getString(
					"serverPassword", "")));
		} catch (StringIndexOutOfBoundsException sioobe) {
			Log.error("Error while trying to deobfuscate the server password");
		}
		try {
			// in order to force connection to the net
			/*
			 * try{ ( new java.net.ServerSocket(80) ).close(); } catch (Exception e) {}
			 */
			ipTxt.setText(InetAddress.getLocalHost().getHostAddress());
		} catch (Exception e1) {
			// Ignore this error
		}
		portTxt.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_network.port.tooltip"));
		ipTxt.setEditable(false);
		ipTxt.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_server.ip.tooltip"));

		// game name of server
		JPanel gameNamePanel = setSizes(new JLabel(LanguageManager
				.getString("wiz_server.gamename")
				+ " : "));
		gameNameTxt = new JTextField(Configuration.getString("gameName", "My Game"));
		gameNameTxt.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_server.gamename.tooltip"));
		gameNameTxt.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE,
				MAXIMAL_TXT_SIZE));
		addCheckValidity(gameNameTxt);
		gameNamePanel.add(gameNameTxt);
		gameParamPanel.add(gameNamePanel, 1);

		// who start option
		JPanel whoStartsPanel = setSizes(new JLabel(LanguageManager
				.getString("wiz_server.whoStarts")
				+ " : "));
		whoStarts = new JComboBox();
		whoStarts
				.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE, MAXIMAL_TXT_SIZE));
		whoStarts.setEditable(false);
		for (StartingOption option : StartingOption.values()) {
			whoStarts.addItem(option.getLocaleValue());
		}
		whoStarts.setSelectedIndex(Configuration.getInt("whoStarts",
				StartingOption.random.ordinal()));
		whoStartsPanel.add(whoStarts);
		gameParamPanel.add(whoStartsPanel);

		// use mana option
		useMana = new JCheckBox(LanguageManager.getString("wiz_server.allowmana"),
				Configuration.getBoolean("useMana", true));
		useMana.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_server.allowmana.tooltip")
				+ "<br><br>" + MagicUIComponents.HTML_ICON_TIP
				+ LanguageManager.getString("wiz_server.allowmana.tooltip.tip"));
		gameParamPanel.add(useMana);

		// opponent response option
		opponentResponse = new JCheckBox(LanguageManager
				.getString("wiz_server.allowopponentresponse"), Configuration
				.getBoolean("opponentResponse", true));
		opponentResponse.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_server.allowopponentresponse.tooltip")
				+ "<br><br>"
				+ MagicUIComponents.HTML_ICON_TIP
				+ LanguageManager
						.getString("wiz_server.allowopponentresponse.tooltip.tip"));
		gameParamPanel.add(opponentResponse);
		checkValidity();
	}

	@Override
	protected boolean checkValidity() {
		if (!super.checkValidity()) {
			return false;
		}
		if (gameNameTxt.getText().length() == 0) {
			wizardInfo.resetWarning(LanguageManager
					.getString("wiz_server.gamename.missed"));
			return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == okBtn) {
			/*
			 * invoked when user create a game
			 */
			try {
				// verify validity of port
				Object value = portTxt.getValue();
				int port = Integer.parseInt(value == null ? portTxt.getText() : portTxt
						.getValue().toString());
				if (port < MINIMAL_PORT || port > MAXIMAL_PORT) {
					// out of range integer{
					JOptionPane.showMessageDialog(this, LanguageManager
							.getString("wiz_network.port.invalid"), LanguageManager
							.getString("error"), JOptionPane.WARNING_MESSAGE);
					return;
				}

				// verify gameNameTxt
				String gameName = gameNameTxt.getText().trim();
				if (gameName.length() == 0) {
					JOptionPane.showMessageDialog(this, LanguageManager
							.getString("wiz_server.gamename.missed"), LanguageManager
							.getString("error"), JOptionPane.WARNING_MESSAGE);
					dispose();
					return;
				}
				if (!validateDeck()) {
					return;
				}

				setVisible(false);

				// all parameters seem to be valid, save them
				Configuration.setProperty("port", port);
				Configuration.setProperty("gameName", gameName);
				Configuration.setProperty("decks.constraint",
						((DeckConstraint) constraintList.getSelectedItem()).getName());
				ConnectionManager.closeConnexions();
				Configuration.setProperty("serverPassword", Password
						.obfuscate(new String(passwordTxt.getPassword())));
				Configuration.setProperty("useMana", useMana.isSelected());
				Configuration.setProperty("opponentResponse", opponentResponse
						.isSelected());
				Configuration.setProperty("whoStarts", whoStarts.getSelectedIndex());

				// Show the game launcher
				LoaderConsole.launchLoader();
				ConnectionManager.server = new net.sf.firemox.network.Server(deck,
						passwordTxt.getPassword());
				ConnectionManager.server.start();
			} catch (Exception e1) {
				ConnectionManager.closeConnexions();
				JOptionPane.showMessageDialog(this, LanguageManager
						.getString("wiz_network.unknownpb")
						+ " : " + e1.getMessage(), LanguageManager
						.getString("wiz_network.unknownpb"), JOptionPane.WARNING_MESSAGE);
				return;
			}
			setVisible(false);
			dispose();
		} else {
			super.actionPerformed(event);
		}
	}

	/**
	 * The "use mana" option.
	 */
	private final JCheckBox useMana;

	/**
	 * The opponent response option.
	 */
	private final JCheckBox opponentResponse;

	/**
	 * The starting player option.
	 */
	private final JComboBox whoStarts;

	/**
	 * The game name text.
	 */
	private final JTextField gameNameTxt;
}
