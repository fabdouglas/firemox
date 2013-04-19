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
 * The available ability identifiers.
 * 
 * @author <a href="mailto:riclas@users.sourceforge.net">Riclas </a>
 */
public interface IdAbilities {

	/**
	 * Activated ability
	 */
	int ANY = 0x00;

	/**
	 * Activated ability
	 */
	int ACTIVATED_ABILITY = 0x01;

	/**
	 * Triggered Ability
	 */
	int TRIGGERED_ABILITY = 0x02;

	/**
	 * The ability name associated to abilities names
	 */
	String[] ABILITIES_NAMES = { "any", "activated", "triggered" };

	/**
	 * The abilities name associated to abilities values (index)
	 */
	int[] ABILITIES_VALUES = { ANY, ACTIVATED_ABILITY, TRIGGERED_ABILITY };

}