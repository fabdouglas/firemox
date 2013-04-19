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
package net.sf.firemox.modifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public enum ModifierType {

	/**
	 * Simple modifier section
	 */
	REGISTER_MODIFIER,

	/**
	 * Id card modifier
	 */
	IDCARD_MODIFIER,

	/**
	 * property modifier
	 */
	PROPERTY_MODIFIER,

	/**
	 * register-indirection modifier
	 */
	REGISTER_INDIRECTION,

	/**
	 * Color modifier
	 */
	COLOR_MODIFIER,

	/**
	 * A static modifier
	 */
	STATIC_MODIFIER,

	/**
	 * Controller modifier
	 */
	CONTROLLER_MODIFIER,

	/**
	 * Playable zone modifier
	 */
	PLAYABLE_ZONE_MODIFIER,

	/**
	 * Ability modifier
	 */
	ABILITY_MODIFIER,

	/**
	 * Object modifier
	 */
	OBJECT_MODIFIER,

	/**
	 * Additional cost modifier
	 */
	ADDITIONAL_COST_MODIFIER;

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
	 * Read and return the enumeration from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given input stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static ModifierType deserialize(InputStream input) throws IOException {
		return values()[input.read()];
	}
}