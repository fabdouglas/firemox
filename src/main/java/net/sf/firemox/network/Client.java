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
package net.sf.firemox.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JOptionPane;

import net.sf.firemox.action.PayMana;
import net.sf.firemox.action.WaitActivatedChoice;
import net.sf.firemox.clickable.target.player.Opponent;
import net.sf.firemox.clickable.target.player.You;
import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.component.LoaderConsole;
import net.sf.firemox.ui.component.TableTop;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.apache.commons.io.IOUtils;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.2c
 */
public class Client extends NetworkActor implements IdMessages, IdZones {

	/**
	 * create a new server for a specified port, play name, nickName and password
	 * required (null if none)
	 * 
	 * @param deck
	 *          the deck of this client
	 * @param passwd
	 *          is the password needed to connect to this play
	 */
	public Client(Deck deck, char[] passwd) {
		super(deck, passwd);

		// Creation of socket connection
		LoaderConsole.beginTask(LanguageManager
				.getString("wiz_network.waitingforopponent"));
	}

	@Override
	public void run() {
		String entree;
		LoaderConsole.beginTask(LanguageManager
				.getString("wiz_network.creatingconnection")
				+ "...", 2);
		InetAddress adr = null;
		try {
			// Connexion au serveur
			adr = InetAddress.getByName(Configuration.getString("ip"));
			clientSocket = new Socket(adr, port);
		} catch (IOException e) {
			// echec de la connexion au serveur
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("wiz_network.cannotconnectto", adr) + ", "
							+ LanguageManager.getString("wiz_network.port") + ":" + port
							+ ". \n" + LanguageManager.getString("wiz_network.port.invalid"),
					LanguageManager.getString("wiz_network.connectionpb"),
					JOptionPane.WARNING_MESSAGE);
			NetworkActor.cancelling = true;
			LoaderConsole.endTask();
		}

		// stopping?
		if (cancelling) {
			cancelConnexion();
			return;
		}
		LoaderConsole.beginTask(
				LanguageManager.getString("wiz_network.connecting"), 5);

		try {
			// Création des flots d'entrée/sortie
			outBin = clientSocket.getOutputStream();
			inBin = clientSocket.getInputStream();

			// need password?
			entree = MToolKit.readString(inBin);
			if (STR_PASSWD.equals(entree)) {
				// a password is need by this server
				if (passwd == null) {
					// ... but we haven't any
					LoaderConsole.beginTask(LanguageManager
							.getString("wiz_network.password.missed"));
					MToolKit.writeString(outBin, STR_NOPASSWD);
					// close stream
					IOUtils.closeQuietly(inBin);
					IOUtils.closeQuietly(outBin);
					// free pointers
					outBin = null;
					inBin = null;
				} else {
					// send our password
					MToolKit.writeString(outBin, new String(passwd));
					entree = MToolKit.readString(inBin);
					if (STR_WRONGPASSWD.equals(entree)) {
						// wrong password
						LoaderConsole.beginTask(LanguageManager
								.getString("wiz_network.password.invalid"));
						// close stream
						IOUtils.closeQuietly(inBin);
						IOUtils.closeQuietly(outBin);
						// free pointers
						outBin = null;
						inBin = null;
					}
				}
			}
			if (outBin != null && !STR_OK.equals(entree)) {
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.unknowncommand")
						+ entree);
				// close stream
				IOUtils.closeQuietly(inBin);
				IOUtils.closeQuietly(outBin);
				// free pointers
				outBin = null;
				inBin = null;
			}
			if (outBin != null) {
				// send our version
				MToolKit.writeString(outBin, IdConst.VERSION);
				entree = MToolKit.readString(inBin);
				if (STR_WRONGVERSION.equals(entree)) {
					// wrong version
					LoaderConsole.beginTask(LanguageManager
							.getString("wiz_network.differentversionpb"));
					// close stream
					IOUtils.closeQuietly(inBin);
					IOUtils.closeQuietly(outBin);
					// free pointers
					outBin = null;
					inBin = null;
				}
			}
			if (outBin != null && !STR_OK.equals(entree)) {
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.unknowncommand")
						+ entree);
				// close stream
				IOUtils.closeQuietly(inBin);
				IOUtils.closeQuietly(outBin);
				// free pointers
				outBin = null;
				inBin = null;
			}

			if (outBin != null) {
				/*
				 * client is connected to the server client/serveur I am ...
				 */
				MToolKit.writeString(outBin, nickName);
				// Opponent is ...
				String serverName = MToolKit.readString(inBin);
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.opponentis")
						+ serverName, 10);

				// exchange shared string settings
				((Opponent) StackManager.PLAYERS[1]).readSettings(serverName, nickName,
						inBin);
				((You) StackManager.PLAYERS[0]).sendSettings(outBin);

				// stopping?
				if (cancelling) {
					cancelConnexion();
					return;
				}

				// receive, and set the random seed
				long seed = Long.parseLong(MToolKit.readString(inBin));
				MToolKit.random.setSeed(seed);
				Log.info("Seed = " + seed);

				// read mana use option
				PayMana.useMana = Integer.parseInt(MToolKit.readString(inBin)) == 1;

				// read opponent response option
				WaitActivatedChoice.opponentResponse = Integer.parseInt(MToolKit
						.readString(inBin)) == 1;

				// Who starts?
				final StartingOption startingOption = StartingOption.values()[Integer
						.valueOf(MToolKit.readString(inBin)).intValue()];
				final boolean serverStarts;
				switch (startingOption) {
				case random:
				default:
					serverStarts = MToolKit.random.nextBoolean();
					break;
				case server:
					serverStarts = true;
					break;
				case client:
					serverStarts = false;
				}

				if (serverStarts) {
					// server begins
					LoaderConsole.beginTask(LanguageManager
							.getString("wiz_network.opponentwillstart")
							+ " (mode=" + startingOption.getLocaleValue() + ")", 15);
					StackManager.idActivePlayer = 1;
					StackManager.idCurrentPlayer = 1;
				} else {
					// client begins
					LoaderConsole.beginTask(LanguageManager
							.getString("wiz_network.youwillstarts")
							+ " (mode=" + startingOption.getLocaleValue() + ")", 15);
					StackManager.idActivePlayer = 0;
					StackManager.idCurrentPlayer = 0;
				}

				// load rules from the MDB file
				dbStream = MdbLoader.loadMDB(MToolKit.mdbFile,
						StackManager.idActivePlayer);
				TableTop.getInstance().initTbs();

				// send our deck
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.sendingdeck"), 25);
				deck.send(outBin);
				StackManager.PLAYERS[0].zoneManager.giveCards(deck, dbStream);
				MToolKit.writeString(outBin, "%EOF%");
				outBin.flush();

				// stopping?
				if (cancelling) {
					cancelConnexion();
					return;
				}

				// receive her/his deck
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.receivingdeck"), 55);
				readAndValidateOpponentDeck();

				// free resources
				LoaderConsole.setTaskPercent(100);

				// stopping?
				if (cancelling) {
					cancelConnexion();
					return;
				}

				// stopping?
				if (cancelling) {
					cancelConnexion();
					return;
				}

				initBigPipe();
				MagicUIComponents.magicForm.initGame();
			}
		} catch (Throwable e) {
			NetworkActor.cancelling = true;
			LoaderConsole.endTask();
			cancelConnexion();
			Log.error(e);
			throw new RuntimeException(LanguageManager.getString(
					"wiz_network.badconnection", adr), e);
		}
	}
}