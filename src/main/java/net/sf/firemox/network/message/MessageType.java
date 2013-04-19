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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.firemox.tools.Log;

/**
 * Message type throw the stream of linked instances.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public enum MessageType {

	/**
	 * A core message.
	 */
	CORE,

	/**
	 * A chat message.
	 */
	CHAT;

	/**
	 * Return the message from the given input.
	 * 
	 * @param input
	 *          the input stream containing the message type code.
	 * @return the message from the given input.
	 * @throws IOException
	 *           if the message type code could not be read.
	 */
	public static MessageType valueOf(InputStream input) throws IOException {
		final int type = input.read();
		if (type < 0 || type > MessageType.values().length) {
			Log.fatal("An unknow message type '" + type
					+ "' has been received. Disconnection is required");
		}
		return MessageType.values()[type];
	}

	/**
	 * Read and return a new Message instance built from this message type and the
	 * data read from the given input.
	 * 
	 * @param input
	 *          the input stream containing the data.
	 * @return a new Message instance built from this message type and the data
	 *         read from the given input.
	 * @throws IOException
	 *           if any message could not be read.
	 */
	public Message readMessage(InputStream input) throws IOException {
		switch (this) {
		case CORE:
			return new CoreMessage(input);
		case CHAT:
			return new ChatMessage(input);
		default:
			Log.fatal("The message type '" + this + "' is not yet implemented");
		}
		return null;
	}

	/**
	 * Write this message type to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void write(OutputStream out) throws IOException {
		out.write(ordinal());
	}

}
