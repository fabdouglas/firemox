/*
 * Created on Nov 2, 2004 
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
package net.sf.firemox.tools;

import net.sf.firemox.clickable.target.card.MCard;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class PairCardInt {

	/**
	 * The stored card instance.
	 */
	public MCard card;

	/**
	 * The stored value.
	 */
	public int value;

	/**
	 * Creates a new instance of PairCardInt <br>
	 * 
	 * @param card
	 * @param value
	 */
	public PairCardInt(MCard card, int value) {
		this.card = card;
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof PairCardInt && ((PairCardInt) o).card == card
				&& ((PairCardInt) o).value == value;
	}

	@Override
	public int hashCode() {
		return card.hashCode();
	}
}
