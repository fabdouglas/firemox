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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.deckbuilder.DeckReader;
import net.sf.firemox.network.message.CoreMessage;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.component.LoaderConsole;

import org.apache.commons.io.IOUtils;

/**
 * This class is representing a client or a server, so contains nickname,
 * connection's port and streams of opened socket.
 * 
 * @since 0.2d
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public abstract class NetworkActor extends Thread {

	/**
	 * Creates a new instance of NetworkActor <br>
	 * 
	 * @param deck
	 *          the deck of this network component.
	 * @param passwd
	 *          is the password needed to connect to this play
	 */
	protected NetworkActor(Deck deck, char[] passwd) {
		this.nickName = StackManager.PLAYERS[0].getNickName();
		this.deck = deck;
		this.passwd = passwd;
		this.port = Configuration.getInt("port", 1299);
		ConnectionManager.enableConnectingTools(true);
		bigPipe = null;
		cancelling = false;
		freePlaces();
	}

	/**
	 * Initialize the pipe.
	 */
	protected void initBigPipe() {
		if (bigPipe != null) {
			// is already running!
			return;
		}
		bigPipe = new MBigPipe(inBin, outBin);
		bigPipe.start();
	}

	/**
	 * end this server, close all connections with connected clients
	 */
	public void closeConnexion() {
		MagicUIComponents.timer.stop();
		cancelling = true;
		try {
			// close stream
			if (clientSocket != null) {
				clientSocket.close();
			}
			IOUtils.closeQuietly(outBin);
			IOUtils.closeQuietly(inBin);
			// free pointers
		} catch (Exception e) {
			// Nothing to do
		}
		outBin = null;
		inBin = null;
	}

	/**
	 * flush the buffer or the current OutputStream
	 */
	public void flush() {
		try {
			outBin.flush();
		} catch (IOException e) {
			throw new InternalError("when flush outBin stream");
		}
	}

	/**
	 * Send the specified message to the opponent.
	 * 
	 * @param message
	 *          is the message to send
	 */
	public void send(CoreMessage message) {
		bigPipe.send(message);
	}

	/**
	 * cancel the join/create action, close current connections and set the main
	 * frame visible.
	 */
	public void cancelConnexion() {
		closeConnexion();
		freePlaces();
		LoaderConsole.endTask();
	}

	/**
	 * Remove from all places the existing components and set to zero the mana
	 * pools.
	 */
	private void freePlaces() {
		StackManager.PLAYERS[0].zoneManager.reset();
		StackManager.PLAYERS[1].zoneManager.reset();
	}

	/**
	 * Read and validate the opponent's deck.
	 * 
	 * @return <code>true</code> when the opponent's deck is valid.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	protected boolean readAndValidateOpponentDeck() throws IOException {
		Deck opponentDeck = DeckReader.getDeck(new MInputStream(inBin));
		if (!DeckReader.validateDeck(MagicUIComponents.magicForm, opponentDeck,
				deck.getConstraint().getName())) {
			cancelling = true;
			return false;
		}
		StackManager.PLAYERS[1].zoneManager.giveCards(opponentDeck, dbStream);
		return true;
	}

	/**
	 * the password needed to be connected
	 */
	protected final char[] passwd;

	/**
	 * The nickname of this actor (client or server)
	 */
	protected final String nickName;

	/**
	 * the socket of this connection
	 */
	protected Socket clientSocket;

	/**
	 * Port number
	 */
	protected final int port;

	/**
	 * Deck.
	 */
	protected Deck deck;

	/**
	 * The optional OutputStream of deck.
	 */
	protected OutputStream outBin;

	/**
	 * InputStream of deck. May be remote deck.
	 */
	protected InputStream inBin;

	/**
	 * The opened stream of MDB.
	 */
	protected FileInputStream dbStream;

	private MBigPipe bigPipe;

	/**
	 * Is canceling?
	 */
	public static boolean cancelling;
}