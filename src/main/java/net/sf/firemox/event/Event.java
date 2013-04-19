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
package net.sf.firemox.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface contains all events supported
 * 
 * @since 0.1
 * @see net.sf.firemox.event.EventFactory
 */
public enum Event {

	/**
	 * 
	 */
	BECOMING_UNTAPPED,

	/**
	 * 
	 */
	BEFORE_PHASE,

	/**
	 * 
	 */
	BEGINNING_PHASE,

	/**
	 * 
	 */
	EOP,

	/**
	 * 
	 */
	DETACHED_CARD,

	/**
	 * 
	 */
	EXCEPTION,

	/**
	 * 
	 */
	LETHAL_DAMAGE,

	/**
	 * 
	 */
	UPDATE_LIFE,

	/**
	 * 
	 */
	LOSING_GAME,

	/**
	 * 
	 */
	MODIFIED_PROPERTY,

	/**
	 * 
	 */
	MODIFIED_IDCARD,

	/**
	 * 
	 */
	MODIFIED_IDCOLOR,

	/**
	 * 
	 */
	MODIFIED_OWNER,

	/**
	 * 
	 */
	MODIFIED_CONTROLLER,

	/**
	 * 
	 */
	ATTACHED_TO,

	/**
	 * 
	 */
	MODIFIED_REGISTER,

	/**
	 * 
	 */
	DECLARED_BLOCKING,

	/**
	 * 
	 */
	CASTING,

	/**
	 * 
	 */
	DECLARED_ATTACKING,
	/**
	 * 
	 */
	BECOMING_TAPPED,

	/**
	 * 
	 */
	NEVER_ACTIVATED,

	/**
	 * 
	 */
	UPDATE_TOUGHNESS,

	/**
	 * 
	 */
	TARGETED,

	/**
	 * 
	 */
	CAN_CAST_CARD,

	/**
	 * 
	 */
	DEALTING_DAMAGE,

	/**
	 * 
	 */
	MOVING_CARD,

	/**
	 * 
	 */
	GIVEN_MANA,

	/**
	 * 
	 */
	ARRANGED_ZONE,

	/**
	 * 
	 */
	FACED_UP,

	/**
	 * 
	 */
	FACED_DOWN,

	/**
	 * 
	 */
	MODIFIED_REGISTER_RANGE;

	/**
	 * Write this enumeration to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @throws IOException
	 *           error while writing event's id
	 */
	public void write(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enumeration from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given input stream.
	 * @throws IOException
	 *           error while reading event's id.
	 */
	public static Event valueOf(InputStream input) throws IOException {
		return values()[input.read()];
	}

}