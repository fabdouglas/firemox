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

	int COLORED_MANA_FIRST_INDEX = BLACK_MANA;

	int COLORED_MANA_LAST_INDEX = WHITE_MANA;

	int WHITE_OR_BLUE_MANA = 0x0006;

	int WHITE_OR_BLACK_MANA = 0x0007;

	int BLUE_OR_BLACK_MANA = 0x0008;

	int BLUE_OR_RED_MANA = 0x0009;

	int BLACK_OR_RED_MANA = 0x000A;

	int BLACK_OR_GREEN_MANA = 0x000B;

	int RED_OR_GREEN_MANA = 0x000C;

	int RED_OR_WHITE_MANA = 0x000D;

	int GREEN_OR_WHITE_MANA = 0x000E;

	int GREEN_OR_BLUE_MANA = 0x000F;

	int TWO_OR_BLACK_MANA = 0x0010;

	int TWO_OR_BLUE_MANA = 0x0011;

	int TWO_OR_GREEN_MANA = 0x0012;

	int TWO_OR_RED_MANA = 0x0013;

	int TWO_OR_WHITE_MANA = 0x0014;

	int HYBRID_MANA_FIRST_INDEX = WHITE_OR_BLUE_MANA;

	int HYBRID_MANA_LAST_INDEX = TWO_OR_WHITE_MANA;

	int TWO_COLORS_HYBRID_MANA_FIRST_INDEX = WHITE_OR_BLUE_MANA;

	int TWO_COLORS_HYBRID_MANA_LAST_INDEX = GREEN_OR_BLUE_MANA;

	int COLORLESS_HYBRID_MANA_FIRST_INDEX = TWO_OR_BLACK_MANA;

	int COLORLESS_HYBRID_MANA_LAST_INDEX = TWO_OR_WHITE_MANA;

	int PHYREXIAN_BLACK_MANA = 0x0015;

	int PHYREXIAN_BLUE_MANA = 0x0016;

	int PHYREXIAN_GREEN_MANA = 0x0017;

	int PHYREXIAN_RED_MANA = 0x0018;

	int PHYREXIAN_WHITE_MANA = 0x0019;

	int PHYREXIAN_MANA_FIRST_INDEX = PHYREXIAN_BLACK_MANA;

	int PHYREXIAN_MANA_LAST_INDEX = PHYREXIAN_WHITE_MANA;

	/**
	 * Comment for <code>STATE</code>
	 */
	int STATE = 0x001A;

	/**
	 * Comment for <code>DAMAGE</code>
	 */
	int DAMAGE = 0x001B;

	/**
	 * Comment for <code>DAMAGE</code>
	 */
	int FREE_1 = 0x001C;

	/**
	 * Comment for <code>DAMAGE</code>
	 */
	int FREE_2 = 0x001D;

	/**
	 * Mana colors
	 */
	String[] COLOR_NAMES = { "colorless", "black", "blue", "green", "red",
			"white" };

	/**
	 * Mana colors usable in payable costs.
	 */
	String[] PAYABLE_COLOR_NAMES = { "colorless", "black", "blue", "green",
			"red", "white", "white-or-blue", "white-or-black", "blue-or-black",
			"blue-or-red", "black-or-red", "black-or-green", "red-or-green",
			"red-or-white", "green-or-white", "green-or-blue", "2-or-black",
			"2-or-blue", "2-or-green", "2-or-red", "2-or-white", "pblack", "pblue",
			"pgreen", "pred", "pwhite" };

	/**
	 * Hybrid mana colors.
	 */
	String[] HYBRID_COLOR_NAMES = { "white-or-blue", "white-or-black",
			"blue-or-black", "blue-or-red", "black-or-red", "black-or-green",
			"red-or-green", "red-or-white", "green-or-white", "green-or-blue",
			"2-or-black", "2-or-blue", "2-or-green", "2-or-red", "2-or-white" };

	/**
	 * Phyrexian mana colors.
	 */
	String[] PHYREXIAN_COLOR_NAMES = { "pblack", "pblue", "pgreen", "pred",
			"pwhite" };

}