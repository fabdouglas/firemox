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
package net.sf.firemox.network;

import javax.swing.JOptionPane;

import net.sf.firemox.network.message.CoreMessage;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * Maintains/close connection of connected players.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public final class ConnectionManager {

	/**
	 * Creates a new instance of ConnectionManager <br>
	 */
	private ConnectionManager() {
		super();
	}

	/**
	 * Notify the disconnection with a warning message. this message is displayed
	 * once per disconnection
	 */
	public static void notifyDisconnection() {
		if (connected) {
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("wiz_network.connectionpb"),
					LanguageManager.getString("wiz_network.gamestatus"),
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Close all opened connections
	 * 
	 * @since 0.2c
	 */
	public static void closeConnexions() {
		try {
			try {
				if (connected) {
					connected = false;
					if (MSocketListener.getInstance() != null) {
						MSocketListener.getInstance().closeConnections();
					}
					if (StackManager.idHandedPlayer == 0) {
						MChat.getInstance().sendMessage("");
					}
					enableConnectingTools(false);
					// send to opponent that we leave the play
				}
			} catch (Exception e) {
				// Nothing to do
			}
			if (getNetworkActor() != null) {
				getNetworkActor().closeConnexion();
			}
			// free pointer
			server = null;
			client = null;
			// enable disconnect menu
		} catch (Exception e) {
			// Nothing to do
		}
	}

	/**
	 * Enable/Disable connection ability
	 * 
	 * @param really
	 *          if true, the connection ability is enabled. Disable it otherwise.
	 */
	public static void enableConnectingTools(boolean really) {
		MagicUIComponents.skipMenu.setEnabled(really);
		MagicUIComponents.skipButton.setEnabled(really);
		MagicUIComponents.sendButton.setEnabled(really);
		ConnectionManager.connected = really;
	}

	/**
	 * Send an event to opponent. Since we send an event, we send too (if not
	 * done) settings changed. Data is necessary sent now.
	 * 
	 * @param type
	 *          is the message type.
	 * @param data
	 *          the data.
	 */
	public static void send(CoreMessageType type, byte... data) {
		getNetworkActor().send(new CoreMessage(type, data));
	}

	/**
	 * Return the current network actor.
	 * 
	 * @return the current network actor.
	 */
	public static NetworkActor getNetworkActor() {
		if (client != null) {
			// this is a client
			return client;
		}
		if (server != null) {
			// this is a server
			return server;
		}
		return null;
	}

	/**
	 * this is the server
	 */
	public static Server server = null;

	/**
	 * this is the client (mono client for this version)
	 */
	public static Client client = null;

	/**
	 * Indicates if we are connected
	 */
	private static boolean connected = false;

	/**
	 * Indicates if we are connected to a game.
	 * 
	 * @return true if we are connected to a game.
	 */
	public static boolean isConnected() {
		return connected;
	}

}
