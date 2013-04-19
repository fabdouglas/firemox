/*
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
 * 
 */
package net.sf.firemox.token;

/**
 * The available zone identifiers, and also special codes attached to the state
 * in the zone.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public interface IdZones {

	/**
	 * The play zone
	 */
	int PLAY = 0x00;

	/**
	 * The hand zone
	 */
	int HAND = 0x01;

	/**
	 * The first available additional zone.
	 */
	int FIRST_ADDITIONAL_ZONE = 0x02;

	/**
	 * The last available additional zone.
	 */
	int LAST_ADDITIONAL_ZONE = 0x08;

	/**
	 * The side zone
	 */
	int SIDE = 0x09;

	/**
	 * The stack zone
	 */
	int STACK = 0x0A;

	/**
	 * The delayed buffer zone
	 */
	int DELAYED = 0x0B;

	/**
	 * The triggered buffer zone
	 */
	int TRIGGERED = 0x0C;

	/**
	 * Nb zones
	 */
	int NB_ZONE = TRIGGERED + 1;

	/**
	 * Completly destructed
	 */
	int NOWHERE = 0x0D;

	/**
	 * The zone is determined during the runtime depending on the value on the
	 * context. Cannot be used outside an action in a replacement or triggered
	 * ability.
	 */
	int CONTEXT = 0x0E;

	/**
	 * No care zone
	 */
	int ANYWHERE = 0x0F;

	/**
	 * The must be tapped in play constraint
	 */
	int PLAY_TAPPED = 0x10;

	/**
	 * The must be untapped in play constraint
	 */
	int PLAY_UNTAPPED = 0x20;

	/**
	 * The zones name associated to zone names
	 */
	String[] ZONE_NAMES = { "play", "hand", "side", "stack", "delayed buffer",
			"triggered buffer", "nowhere", "playANDtapped", "playANDuntapped",
			"anywhere", "context" };

	/**
	 * The zones name associated to zone values (index)
	 */
	int[] ZONE_VALUES = { PLAY, HAND, SIDE, STACK, DELAYED, TRIGGERED, NOWHERE,
			PLAY_TAPPED, PLAY_UNTAPPED, ANYWHERE, CONTEXT };

}