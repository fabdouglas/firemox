/*   Firemox is a turn based strategy simulator
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

/**
 * IdCommonToken.java Created on 2 mars 2004
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public interface IdCommonToken {

	/**
	 * Comment for <code>COLORLESS_MANA</code>
	 */
	int COLORLESS_MANA = 0x0000;

	/**
	 * Comment for <code>BLACK_MANA</code>
	 */
	int BLACK_MANA = 0x0001;

	/**
	 * Comment for <code>BLUE_MANA</code>
	 */
	int BLUE_MANA = 0x0002;

	/**
	 * Comment for <code>GREEN_MANA</code>
	 */
	int GREEN_MANA = 0x0003;

	/**
	 * Comment for <code>RED_MANA</code>
	 */
	int RED_MANA = 0x0004;

	/**
	 * Comment for <code>WHITE_MANA</code>
	 */
	int WHITE_MANA = 0x0005;

	/**
	 * Comment for <code>STATE</code>
	 */
	int STATE = 0x0007;

	/**
	 * Comment for <code>DAMAGE</code>
	 */
	int DAMAGE = 0x0008;

	/**
	 * Comment for <code>DAMAGE</code>
	 */
	int FREE_1 = 0x0009;

	/**
	 * Comment for <code>DAMAGE</code>
	 */
	int FREE_2 = 0x000A;

	/**
	 * Mana colors
	 */
	String[] COLOR_NAMES = { "colorless", "black", "blue", "green", "red",
			"white" };

	String[] HYBRID_COLOR_NAMES = { "white-or-blue", "white-or-black",
			"blue-or-black", "blue-or-red", "black-or-red", "black-or-green",
			"red-or-green", "red-or-white", "green-or-white", "green-or-blue",
			"2-or-white", "2-or-blue", "2-or-black", "2-or-red", "2-or-green" };

	String[] PHYREXIAN_COLOR_NAMES = { "pblack", "pblue", "pgreen", "pred",
			"pwhite" };

}