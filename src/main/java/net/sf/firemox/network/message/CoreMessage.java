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

import net.sf.firemox.network.MSocketListener;

/**
 * A message containing some data about an event on the other connected instance
 * of this application.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class CoreMessage implements Message {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param input
	 *          the input stream containing data attached to this message.
	 * @throws IOException
	 *           when the message type or data could not be read.
	 */
	public CoreMessage(InputStream input) throws IOException {
		type = CoreMessageType.valueOf(input);
		data = new byte[input.read()];
		input.read(data);
	}

	/**
	 * Create a new instance of this class.
	 * 
	 * @param type
	 *          the message type.
	 * @param data
	 *          data attached to this message.
	 */
	public CoreMessage(CoreMessageType type, byte... data) {
		this.type = type;
		this.data = data;
	}

	/**
	 * Return data attached to this message.
	 * 
	 * @return data attached to this message.
	 */
	public byte[] getData() {
		return this.data;
	}

	/**
	 * Return the message type.
	 * 
	 * @return the message type.
	 */
	public CoreMessageType getType() {
		return this.type;
	}

	public void write(OutputStream out) throws IOException {
		type.write(out);
		out.write(this.data.length);
		out.write(this.data);
	}

	public void process() {
		MSocketListener.getInstance().stack(this);
	}

	/**
	 * The data attached to this message.
	 */
	private final byte[] data;

	/**
	 * The message type.
	 */
	private final CoreMessageType type;

}
