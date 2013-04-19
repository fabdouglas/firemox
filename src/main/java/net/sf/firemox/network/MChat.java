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

import net.sf.firemox.network.message.ChatMessage;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;

/**
 * This chat is a thread looking for a chat data in the big pipe Display
 * discussion is : $user - $message When a null length message is sent, that
 * would say "disconnection" since user can't send a null length message. So if
 * user want to send a null length message nothing is sent. When disconnection
 * is detected, a message like " **disconnection **" is printed into the history
 * text area.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.3
 */
public class MChat {

	/**
	 * Create a new instance private of this class.
	 */
	private MChat() {
		super();
	}

	/**
	 * Send a specified message to opponent and display it into our chat.
	 * 
	 * @param message
	 *          is the specified message to send.
	 */
	public void sendMessage(String message) {
		try {
			MagicUIComponents.chatHistoryText.append(0, message);
			MBigPipe.instance.send(new ChatMessage(message));
		} catch (Throwable t) {
			MBigPipe.instance.send(new ChatMessage("<i>" + message
					+ "</i> could not be sent : " + t.getMessage()));
			Log.error("Message could not be sent", t);
		}
	}

	/**
	 * To receive a message.
	 * 
	 * @param message
	 *          the input message.
	 */
	public void receiveMessage(ChatMessage message) {
		MagicUIComponents.chatHistoryText.append(1, message.getText());
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static MChat getInstance() {
		return instance;
	}

	/**
	 * Unique instance of this class.
	 */
	private final static MChat instance = new MChat();
}