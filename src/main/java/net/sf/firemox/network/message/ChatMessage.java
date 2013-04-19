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
package net.sf.firemox.network.message;

import java.io.InputStream;
import java.io.OutputStream;

import net.sf.firemox.network.MChat;
import net.sf.firemox.tools.MToolKit;

/**
 * A chat message sent by the other connected instance of this application.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class ChatMessage implements Message {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param input
	 *          the input stream containing data attached to this message.
	 */
	public ChatMessage(InputStream input) {
		text = MToolKit.readText(input);
	}

	/**
	 * Create a new instance of this class with a given text.
	 * 
	 * @param message
	 *          the text message.
	 */
	public ChatMessage(String message) {
		text = message;
	}

	/**
	 * Return data attached to this message.
	 * 
	 * @return data attached to this message.
	 */
	public String getText() {
		return this.text;
	}

	public void process() {
		MChat.getInstance().receiveMessage(this);
	}

	public void write(OutputStream out) {
		MToolKit.writeString(out, text);
	}

	/**
	 * The text attached to this message.
	 */
	private final String text;

}
