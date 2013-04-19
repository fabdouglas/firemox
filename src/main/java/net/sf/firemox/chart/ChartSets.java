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

import java.util.EnumMap;
import java.util.Map;

import net.sf.firemox.chart.datasets.Dataset;
import net.sf.firemox.clickable.target.card.CardModel;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class ChartSets {

	private final Map<ChartFilter, Dataset> datasets;

	/**
	 * Create a new instance of this class.
	 */
	public ChartSets() {
		datasets = new EnumMap<ChartFilter, Dataset>(ChartFilter.class);
	}

	/**
	 * Add a data set to this set.
	 * 
	 * @param filter
	 * @param dataSet
	 */
	public void addDataSet(ChartFilter filter, Dataset dataSet) {
		datasets.put(filter, dataSet);
	}

	/**
	 * Add cards to all data sets.
	 * 
	 * @param cardModel
	 *          the card to add.
	 * @param amount
	 *          the amount of card to add.
	 */
	public void addCard(final CardModel cardModel, final int amount) {
		for (Dataset dataset : datasets.values())
			dataset.addCard(cardModel, amount);
	}

	/**
	 * Remove cards to all data sets.
	 * 
	 * @param cardModel
	 *          the card to remove.
	 * @param amount
	 *          the amount of card to remove.
	 */
	public void removeCard(final CardModel cardModel, final int amount) {
		for (Dataset dataset : datasets.values())
			dataset.removeCard(cardModel, amount);
	}

	/**
	 * @param filter
	 * @param key
	 * @param amount
	 */
	public void initKey(ChartFilter filter, IChartKey key, Integer amount) {
		datasets.get(filter).setValue(key, amount);
	}

	/**
	 * Remove all data from this data set.
	 */
	public void removeAll() {
		for (Dataset dataset : datasets.values())
			dataset.removeAll();
	}
}
