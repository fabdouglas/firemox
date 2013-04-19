/*
 * Created on Jul 27, 2004 
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
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public enum AbilityType {

	/**
	 * This code indicates the following ability is a triggered one.
	 * 
	 * @see net.sf.firemox.clickable.ability.TriggeredAbility
	 */
	TRIGGERED_ABILITY,

	/**
	 * This code indicates the following ability is a triggered one.
	 * 
	 * @see net.sf.firemox.clickable.ability.TriggeredAbilitySet
	 */
	TRIGGERED_ABILITY_SET,

	/**
	 * This code indicates the following ability is an activated one.
	 * 
	 * @see net.sf.firemox.clickable.ability.ActivatedAbility
	 */
	ACTIVATED_ABILITY,

	/**
	 * This code indicates the following ability is an activated one. Any player
	 * can play this ability.
	 * 
	 * @see net.sf.firemox.clickable.ability.ActivatedAbilityPlayer
	 */
	ACTIVATED_ABILITY_PLAYER,

	/**
	 * This code indicates the following ability is a reference.
	 */
	REFERENCED_ABILITY,

	/**
	 * This code indicates the following ability is a replacement ability.
	 * 
	 * @see net.sf.firemox.clickable.ability.ReplacementAbility
	 */
	REPLACEMENT_ABILITY,

	/**
	 * This code indicates the following ability is a system ability.
	 * 
	 * @see net.sf.firemox.clickable.ability.SystemAbility
	 */
	SYSTEM_ABILITY;

	/**
	 * Wrtite this enum to the given output stream.
	 * 
	 * @param out
	 *          the stream ths enum would be written.
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enum from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enum to read.
	 * @return the enum from the given input stream.
	 * @throws IOException
	 */
	static AbilityType valueOf(InputStream input) throws IOException {
		return values()[input.read()];
	}
}