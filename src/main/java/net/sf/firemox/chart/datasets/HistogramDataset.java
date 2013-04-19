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

import java.util.Collection;

import org.jfree.data.statistics.HistogramType;

import net.sf.firemox.chart.ChartFilter;
import net.sf.firemox.chart.IChartKey;
import net.sf.firemox.chart.IDataProvider;
import net.sf.firemox.clickable.target.card.CardModel;

/**
 * 
 */
public class HistogramDataset extends
		org.jfree.data.statistics.HistogramDataset implements Dataset {

	private int[] values = new int[0];

	/**
	 * Create a new instance of this class.
	 * 
	 * @param provider
	 *          the key provider.
	 * @param filter
	 *          the filter attached to this data set.
	 */
	public HistogramDataset(IDataProvider provider, ChartFilter filter) {
		super();
		this.provider = provider;
		this.filter = filter;
		this.setType(HistogramType.FREQUENCY);
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
		Collection<IChartKey> keys = provider.getKeys(cardModel, filter);
		for (IChartKey key : keys) {
			if (key.getIntegerKey() + 1 > values.length) {
				int[] tmp = new int[key.getIntegerKey() + 1];
				System.arraycopy(values, 0, tmp, 0, values.length);
				values = tmp;
			}
			values[key.getIntegerKey()] += amount;
		}
		list.clear();
		int max = 0;
		for (int value : values) {
			if (value > max)
				max = value;
		}
		double[] amounts = new double[max + 1];
		for (int index = max + 1; index-- > 0;) {
			amounts[values[index]] = index;
		}
		try {
			addSeries("data1", amounts, amounts.length, 0, amounts.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireDatasetChanged();
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
		Collection<IChartKey> keys = provider.getKeys(cardModel, filter);
		for (IChartKey key : keys) {
			values[key.getIntegerKey()] -= amount;
			double max = 0;
			for (double value : values)
				if (value > max)
					max = value;
			list.clear();
			try {
				// addSeries("data1", values, 100, 0, max);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		fireDatasetChanged();
	}

	public void setValue(IChartKey key, Integer value) {
		// super.setValue(value, "My Key", key);
	}

	public void removeAll() {
		//
	}

	private final IDataProvider provider;

	private final ChartFilter filter;

}