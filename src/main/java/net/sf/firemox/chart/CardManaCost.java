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
package net.sf.firemox.chart;

import java.util.List;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class CardManaCost implements IChartKey {

	/**
	 * 
	 */
	public static final IChartKey UNKNOW_MANACOST = new CardManaCost(0);

	private final int amount;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param amount
	 *          mana cost amount.
	 */
	public CardManaCost(int amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return String.valueOf(amount);
	}

	public IChartKey getDefault() {
		return UNKNOW_MANACOST;
	}

	public int compareTo(IChartKey o) {
		return Integer.valueOf(amount).compareTo(amount);
	}

	public int getIntegerKey() {
		return amount;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof CardManaCost
				&& ((CardManaCost) other).amount == amount;
	}

	@Override
	public int hashCode() {
		return amount;
	}

	public void processAdd(List<IChartKey> workingKeys) {
		int from = workingKeys.size();
		for (int i = from; i < amount; i++)
			workingKeys.add(new CardManaCost(i));
		workingKeys.add(this);
	}
}
