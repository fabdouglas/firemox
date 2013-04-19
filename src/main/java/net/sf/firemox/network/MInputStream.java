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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import net.sf.firemox.tools.MToolKit;

/**
 * @author Fabrice Daugan
 * @since 0.2c
 */
public class MInputStream {

	/**
	 * create a new instance of MInputStream
	 * 
	 * @param inputStream
	 *          is the inputStream where will be read the bytes
	 */
	public MInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
		this.buffIn = null;
		this.outputStream = null;
	}

	/**
	 * create a new instance of MInputStream
	 * 
	 * @param inputStream
	 *          is the inputStream where will be read the bytes
	 * @param outputStream
	 *          is the OutputStream where will be sent the bytes
	 */
	public MInputStream(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = null;
		this.buffIn = new BufferedReader(new InputStreamReader(inputStream));
		this.outputStream = outputStream;
	}

	/**
	 * read a line from input stream
	 * 
	 * @return the line read from the input stream
	 */
	public String readLine() {
		try {
			if (buffIn != null) {
				return buffIn.readLine();
			}
			return MToolKit.readString(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * write a specified line in the outputStream stream.
	 * 
	 * @param line
	 *          is the string to write
	 */
	public void sendLine(String line) {
		if (outputStream != null) {
			MToolKit.writeString(outputStream, line);
		}
	}

	/**
	 * flush the current output stream
	 */
	public void flush() {
		try {
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * the current input stream
	 */
	private final InputStream inputStream;

	/**
	 * the buffer where bytes are written
	 */
	private final OutputStream outputStream;

	private final BufferedReader buffIn;
}