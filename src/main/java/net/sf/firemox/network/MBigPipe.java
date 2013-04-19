/*
 * MBigPipe.java 
 * Created on 11 nov. 2003
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
 */
package net.sf.firemox.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.firemox.network.message.ChatMessage;
import net.sf.firemox.network.message.CoreMessage;
import net.sf.firemox.network.message.Message;
import net.sf.firemox.network.message.MessageType;
import net.sf.firemox.tools.Log;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.3
 */
public class MBigPipe extends Thread implements IdMessages {

	MBigPipe(InputStream mainIn, OutputStream mainOut) {
		this.mainIn = mainIn;
		this.mainOut = mainOut;
		MSocketListener.init();
		ready = new MMiniPipe();
		ready.take();
		pipeOut = new MMiniPipe();
		pipeInSync = new MMiniPipe();
		pipeInSync.take();
		instance = this;
	}

	/**
	 * Semaphore to indicate a ready stream
	 */
	public MMiniPipe ready;

	/**
	 * send a byte sized message to opponent. This message is not displayed in the
	 * chat window, but is destined to play triggers.
	 * 
	 * @param message
	 *          the message to send
	 */
	public void send(CoreMessage message) {
		pipeOut.take();
		try {

			// Write the message type
			MessageType.CORE.write(mainOut);

			// Write the content
			message.write(mainOut);

			// Flush this message
			mainOut.flush();
		} catch (IOException e) {
			ConnectionManager.notifyDisconnection();
			ConnectionManager.closeConnexions();
			throw new InternalError("disconnected in sendGameMessage");
		} finally {
			pipeOut.release();
		}
	}

	/**
	 * send a STRING message to opponent. This message will be displayed in the
	 * chat window.
	 * 
	 * @param message
	 *          the message to send.
	 */
	public void send(ChatMessage message) {
		pipeOut.take();
		try {

			// Write the message type
			MessageType.CHAT.write(mainOut);

			// Write the content
			message.write(mainOut);

			// Flush this message
			mainOut.flush();
		} catch (IOException e) {
			throw new InternalError("disconnected in sendChatMessage");
		} finally {
			pipeOut.release();
		}
	}

	/**
	 * The main loop allowing to mix 2 data types : the first is the data
	 * corresponding to player's action, and the second is the message of chat
	 * speaking.
	 */
	@Override
	public void run() {
		try {
			while (true) {
				final MessageType type = MessageType.valueOf(mainIn);
				final Message message = type.readMessage(mainIn);
				message.process();
			}
		} catch (IOException e) {
			Log.info(" **disconnected **");
			ConnectionManager.notifyDisconnection();
			// disconnection
			ConnectionManager.closeConnexions();
			return;
		}
	}

	/**
	 * monitors
	 */
	private MMiniPipe pipeOut;

	private MMiniPipe pipeInSync;

	/**
	 * main input stream
	 */
	private InputStream mainIn;

	/**
	 * main output stream
	 */
	private OutputStream mainOut;

	/**
	 * The unique instance
	 */
	public static MBigPipe instance;
}