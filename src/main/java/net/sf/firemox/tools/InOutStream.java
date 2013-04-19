/*
 * Created on Aug 6, 2004 
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
package net.sf.firemox.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class InOutStream extends InputStream {

	/**
	 * Creates a new instance of InOutStream <br>
	 * 
	 * @param in
	 * @param out
	 */
	public InOutStream(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}

	@Override
	public int read() throws IOException {
		int read = in.read();
		if (read == -1) {
			throw new IOException("InOutStream exception");
		}
		out.write(read);
		return read;
	}

	private InputStream in;

	private OutputStream out;
}