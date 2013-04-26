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

import java.awt.Color;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public interface IdCardColors {

	/**
	 * Card color names Value = Card Colors 2^index
	 */
	String[] CARD_COLOR_NAMES = { "colorless", "black", "blue", "green", "red",
			"white" };

	/**
	 * Card color names for hybrid mana
	 */
	String[] HYBRID_COLOR_NAMES = { "white-or-blue", "white-or-black",
			"blue-or-black", "blue-or-red", "black-or-red", "black-or-green",
			"red-or-green", "red-or-white", "green-or-white", "green-or-blue",
			"2-or-black", "2-or-blue", "2-or-green", "2-or-red", "2-or-white" };

	/**
	 * Card color names for phyrexian mana
	 */
	String[] PHYREXIAN_COLOR_NAMES = { "pblack", "pblue", "pgreen", "pred",
			"pwhite" };

	/**
	 * Card color names Value = Card Colors 2^index
	 */
	Color[] CARD_COLOR = { Color.GRAY, Color.BLACK, Color.BLUE, Color.GREEN,
			Color.RED, Color.WHITE };

	/**
	 * Card color values
	 */
	int[] CARD_COLOR_VALUES = { 0x00, 0x01, 0x02, 0x04, 0x08, 0x10 };

	/**
	 * Card color values for hybrid manas
	 */
	int[] HYBRID_COLOR_VALUES = { 0x12, 0x11, 0x03, 0x0A, 0x09, 0x05, 0x0C, 0x18,
			0x14, 0x06, 0x10, 0x02, 0x01, 0x08, 0x04 };

	/**
	 * Card color values for phyrexian manas
	 */
	int[] PHYREXIAN_COLOR_VALUES = { 0x01, 0x02, 0x04, 0x08, 0x10 };

	int[][] HYBRID_PHYREXIAN_MANA_ALTERNATIVES = {
			{ 0, 0, 1, 0, 0, 1, 0 },
			{ 0, 1, 0, 0, 0, 1, 0 },
			{ 0, 1, 1, 0, 0, 0, 0 },
			{ 0, 0, 1, 0, 1, 0, 0 },
			{ 0, 1, 0, 0, 1, 0, 0 },
			{ 0, 1, 0, 1, 0, 0, 0 },
			{ 0, 0, 1, 1, 0, 0, 0 },
			{ 0, 0, 0, 0, 1, 1, 0 },
			{ 0, 0, 0, 1, 0, 1, 0 },
			{ 0, 0, 1, 1, 0, 0, 0 },
			{ 2, 1, 0, 0, 0, 0, 0 },
			{ 2, 0, 1, 0, 0, 0, 0 },
			{ 2, 0, 0, 1, 0, 0, 0 },
			{ 2, 0, 0, 0, 1, 0, 0 },
			{ 2, 0, 0, 0, 0, 1, 0 },
			{ 0, 1, 0, 0, 0, 0, 1 },
			{ 0, 0, 1, 0, 0, 0, 1 },
			{ 0, 0, 0, 1, 0, 0, 1 },
			{ 0, 0, 0, 0, 1, 0, 1 },
			{ 0, 0, 0, 0, 0, 1, 1 }
	};
}