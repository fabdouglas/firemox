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
 * 
 */
package net.sf.firemox.chart.datasets;

import net.sf.firemox.chart.IChartKey;
import net.sf.firemox.clickable.target.card.CardModel;

/**
 * 
 */
public interface Dataset {

	/**
	 * Add cards to all datasets.
	 * 
	 * @param cardModel
	 *          the card to add.
	 * @param amount
	 *          the amount of card to add.
	 */
	void addCard(final CardModel cardModel, final int amount);

	/**
	 * Remove cards to all datasets.
	 * 
	 * @param cardModel
	 *          the card to remove.
	 * @param amount
	 *          the amount of card to remove.
	 */
	void removeCard(final CardModel cardModel, final int amount);

	/**
	 * Sets the data value for a key and sends a
	 * {@link org.jfree.data.general.DatasetChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param key
	 *          the key (<code>null</code> not permitted).
	 * @param value
	 *          the value.
	 */
	void setValue(IChartKey key, Integer value);

	/**
	 * Remove all data from this dataset.
	 */
	void removeAll();

}