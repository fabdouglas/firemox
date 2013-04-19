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

/**
 * A message type can be sent over the connected stream of two running instance
 * of this application.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public enum CoreMessageType {

	/**
	 * The player has selected a choice in a Choice input.
	 */
	CHOICE,

	/**
	 * The player has clicked on a card.
	 */
	CLICK_CARD,

	/**
	 * Some disconnection message has been sent by a player.
	 */
	DISCONNECT,

	/**
	 * The player has selected a mana.
	 */
	CLICK_MANA,

	/**
	 * The player has selected a mana to pay mana.
	 */
	CLICK_PAY_MANA,

	/**
	 * The player has selected a player.
	 */
	CLICK_PLAYER,

	/**
	 * The player has selected a replacement ability
	 */
	REPLACEMENT_ANSWER,

	/**
	 * The player has selected the move order.
	 */
	MOVE_ORDER_ANSWER,

	/**
	 * The player has entered a number.
	 */
	INTEGER_ANSWER,

	/**
	 * The player has closed a single input.
	 */
	ANSWER,

	/**
	 * The player has declined/skipped
	 */
	SKIP,

	/**
	 * The player has selected an ability.
	 */
	CLICK_ABILITY,

	/**
	 * The player has selected a triggered ability.
	 */
	CLICK_TRIGGERED_CARD,

	/**
	 * The player has chosen the triggered card order.
	 */
	TRIGGERED_CARD_CHOICE,

	/**
	 * The player has selected a color.
	 */
	COLOR_ANSWER,

	/**
	 * The player has selected a zone.
	 */
	ZONE_ANSWER,

	/**
	 * The player has selected a property.
	 */
	PROPERTY_ANSWER,

	/**
	 * The player has selected an action to play in the left panel.
	 */
	CLICK_ACTION;

	/**
	 * Read and return the enumeration from the given stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static CoreMessageType valueOf(InputStream input) throws IOException {
		return CoreMessageType.values()[input.read()];
	}

	/**
	 * Write this enumeration to the given output stream.
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
