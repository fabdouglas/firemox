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

import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.swing.JOptionPane;

import net.sf.firemox.action.PayMana;
import net.sf.firemox.action.WaitActivatedChoice;
import net.sf.firemox.clickable.target.player.Opponent;
import net.sf.firemox.clickable.target.player.You;
import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.deckbuilder.MdbLoader;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.component.LoaderConsole;
import net.sf.firemox.ui.component.TableTop;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.apache.commons.io.IOUtils;

/**
 * a multi-client server
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.2c
 */
public class Server extends NetworkActor implements IdMessages {

	/**
	 * create a new server for a specified port, play name, nickName and password
	 * required (null if none)
	 * 
	 * @param deck
	 *          the deck of this server
	 * @param passwd
	 *          is the password needed to connect to this play
	 */
	public Server(Deck deck, char[] passwd) {
		super(deck, passwd);
		LoaderConsole.beginTask(LanguageManager
				.getString("wiz_network.waitingforopponent"));
	}

	/**
	 * If this thread was constructed using a separate <code>Runnable</code> run
	 * object, then that <code>Runnable</code> object's <code>run</code>
	 * method is called; otherwise, this method does nothing and returns.
	 * <p>
	 * Subclasses of <code>Thread</code> should override this method.
	 * 
	 * @see java.lang.Thread#start()
	 * @see java.lang.Thread#Thread(java.lang.ThreadGroup, java.lang.Runnable,
	 *      java.lang.String)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// Creating connection socket
		try {
			serverSocket = new ServerSocket(port, 1);
			// enable timeout
			serverSocket.setSoTimeout(2000);

			// accept any client
			clientSocket = null;
			LoaderConsole.beginTask(LanguageManager
					.getString("wiz_network.creatingconnection"), 2);
			while (clientSocket == null && !cancelling) {
				try {
					clientSocket = serverSocket.accept();
				} catch (SocketException timeout) {
					if (!"socket closed".equals(timeout.getMessage())) {
						throw timeout;
					}
				} catch (SocketTimeoutException timeout) {
					/*
					 * timeout of 'accept()' method, nothing to do, we look if we're still
					 * running
					 */
				}
			}

			// stopping?
			if (cancelling) {
				Log.info(LanguageManager.getString("wiz_network.canceledcreation"));
				cancelConnexion();
				return;
			}

			// A client is connecting...
			LoaderConsole.beginTask(LanguageManager
					.getString("wiz_network.incomming")
					+ " : " + clientSocket.getInetAddress().toString(), 5);

			// free these two sockets later
			outBin = clientSocket.getOutputStream();
			inBin = clientSocket.getInputStream();
			// socketListener = new SocketListener(inBin);

			if (passwd != null && passwd.length > 0) {
				// a password is needed to connect to this server
				MToolKit.writeString(outBin, STR_PASSWD);
				if (MToolKit.readString(inBin).equals(new String(passwd))) {
					MToolKit.writeString(outBin, STR_OK);
				} else {
					// wrong password, this client client will be disconnected
					MToolKit.writeString(outBin, STR_WRONGPASSWD);
					// close stream
					IOUtils.closeQuietly(inBin);
					IOUtils.closeQuietly(outBin);
					// free pointers
					outBin = null;
					inBin = null;
				}
			} else {
				MToolKit.writeString(outBin, STR_OK);
			}

			// check version of client
			String clientVersion = MToolKit.readString(inBin);
			if (IdConst.VERSION.equals(clientVersion)) {
				MToolKit.writeString(outBin, STR_OK);
			} else {
				// two different versions
				MToolKit.writeString(outBin, STR_WRONGVERSION);
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.differentversionClientpb")
						+ " (" + clientVersion + ")", 10);
				IOUtils.closeQuietly(inBin);
				IOUtils.closeQuietly(outBin);
				// free pointers
				outBin = null;
				inBin = null;
			}

			if (outBin != null) {

				// enter in the main loop
				// Opponent is ...
				String clientName = MToolKit.readString(inBin);
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.opponentis")
						+ clientName, 10);
				// I am ...
				MToolKit.writeString(outBin, nickName);

				// exchange shared string settings
				((You) StackManager.PLAYERS[0]).sendSettings(outBin);
				((Opponent) StackManager.PLAYERS[1]).readSettings(clientName, nickName,
						inBin);

				// stopping?
				if (cancelling) {
					cancelConnexion();
					return;
				}

				// set and send the random seed
				long seed = MToolKit.random.nextLong();
				MToolKit.random.setSeed(seed);
				MToolKit.writeString(outBin, Long.toString(seed));
				Log.info("Seed = " + seed);

				// write mana use option
				PayMana.useMana = Configuration.getBoolean("useMana", true);
				MToolKit.writeString(outBin, PayMana.useMana ? "1" : "0");

				// write opponent response option
				WaitActivatedChoice.opponentResponse = Configuration.getBoolean(
						"opponnentresponse", true);
				MToolKit.writeString(outBin, WaitActivatedChoice.opponentResponse ? "1"
						: "0");

				// Who starts?
				final StartingOption startingOption = StartingOption.values()[Configuration
						.getInt("whoStarts", StartingOption.random.ordinal())];
				MToolKit.writeString(outBin, String.valueOf(Configuration.getInt(
						"whoStarts", 0)));
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
							.getString("wiz_network.youwillstarts")
							+ " (mode=" + startingOption.getLocaleValue() + ")", 15);
					StackManager.idActivePlayer = 0;
					StackManager.idCurrentPlayer = 0;
				} else {
					// client begins
					LoaderConsole.beginTask(LanguageManager
							.getString("wiz_network.opponentwillstart")
							+ " (mode=" + startingOption.getLocaleValue() + ")", 15);
					StackManager.idActivePlayer = 1;
					StackManager.idCurrentPlayer = 1;
				}

				// load rules from the MDB file
				dbStream = MdbLoader.loadMDB(MToolKit.mdbFile,
						StackManager.idActivePlayer);
				TableTop.getInstance().initTbs();

				// receive and validate her/his deck
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.receivingdeck"), 25);
				readAndValidateOpponentDeck();

				// stopping?
				if (cancelling) {
					cancelConnexion();
					return;
				}

				// send our deck
				LoaderConsole.beginTask(LanguageManager
						.getString("wiz_network.sendingdeck"), 55);
				deck.send(outBin);
				StackManager.PLAYERS[0].zoneManager.giveCards(deck, dbStream);

				// stopping?
				if (cancelling) {
					cancelConnexion();
					return;
				}

				MToolKit.writeString(outBin, "%EOF%");

				// free resources
				outBin.flush();

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
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("wiz_server.error") + " : "
							+ e.getMessage(), LanguageManager.getString("error"),
					JOptionPane.WARNING_MESSAGE);
			Log.error(e);
			return;
		}
	}

	@Override
	public void closeConnexion() {
		super.closeConnexion();
		// close stream
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (Exception e) {
				// Nothing to do
			}
		}
	}

	/**
	 * the server's socket of this connexion
	 */
	public ServerSocket serverSocket;

}