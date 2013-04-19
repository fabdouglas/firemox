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
package net.sf.firemox.token;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Boolean option including 'unset' value.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
public enum BooleanOption {
	/**
	 * This option has not been set.
	 */
	UNSET,

	/**
	 * This option has "true" value.
	 */
	TRUE,

	/**
	 * This option has "false" value.
	 */
	FALSE;

	/**
	 * Write the enumeration corresponding to the given XSD name to the given
	 * output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @param xsdName
	 *          the XSD name of this TestOn.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void serialize(OutputStream out, String xsdName)
			throws IOException {
		final BooleanOption value = valueOfXsd(xsdName);
		if (value == null) {
			throw new IllegalArgumentException("Invalid xsd attribute name : "
					+ xsdName);
		}
		value.serialize(out);
	}

	/**
	 * Write this enumeration to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void serialize(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enumeration from the given stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static BooleanOption deserialize(InputStream input) throws IOException {
		return values()[input.read()];
	}

	/**
	 * Return null of enumeration value corresponding to the given XSD name.
	 * 
	 * @param xsdName
	 *          the XSD name of this abstract value.
	 * @return null of enumeration value corresponding to the given XSD name.
	 */
	public static BooleanOption valueOfXsd(String xsdName) {
		if (xsdName == null || xsdName.length() == 0) {
			return UNSET;
		}
		for (BooleanOption value : values()) {
			if (value.name().equalsIgnoreCase(xsdName)) {
				return value;
			}
		}
		return UNSET;
	}

	/**
	 * Return the boolean value. <code>null</code> for UNSET value.
	 * 
	 * @return the boolean value. <code>null</code> for UNSET value.
	 */
	public Boolean getValue() {
		switch (this) {
		case TRUE:
			return Boolean.TRUE;
		case FALSE:
			return Boolean.FALSE;
		case UNSET:
		default:
			return null;
		}
	}

}
