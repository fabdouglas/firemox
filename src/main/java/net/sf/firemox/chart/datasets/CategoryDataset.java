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

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.chart.ChartFilter;
import net.sf.firemox.chart.IChartKey;
import net.sf.firemox.chart.IDataProvider;
import net.sf.firemox.clickable.target.card.CardModel;

import org.jfree.data.category.DefaultCategoryDataset;

/**
 * 
 */
public class CategoryDataset extends DefaultCategoryDataset implements Dataset {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param provider
	 *          the key provider.
	 * @param filter
	 *          the filter attached to this data set.
	 */
	public CategoryDataset(IDataProvider provider, ChartFilter filter) {
		super();
		this.provider = provider;
		this.filter = filter;
		this.workingKeys = new ArrayList<IChartKey>();
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
		for (IChartKey key : provider.getKeys(cardModel, filter)) {
			if (workingKeys.contains(key)) {
				try {
					setValue(key, Integer.valueOf(getValue("My Key", key).intValue()
							+ amount));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				int oldSize = workingKeys.size();
				key.processAdd(workingKeys);
				for (int i = oldSize; i < workingKeys.size() - 1; i++)
					setValue(Integer.valueOf(0), "My Key", workingKeys.get(i));
				setValue(Integer.valueOf(amount), "My Key", key);
			}
		}
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
		try {
			for (IChartKey key : provider.getKeys(cardModel, filter)) {
				final int value = Math.max(getValue("My Key", key).intValue() - amount,
						0);
				setValue(key, value);
				super.validateObject();
			}
		} catch (Exception e) {
			//
			e.printStackTrace();
		}
	}

	public void removeAll() {
		for (IChartKey key : workingKeys)
			super.removeColumn(key);
		workingKeys.clear();
	}

	public void setValue(IChartKey key, Integer value) {
		super.setValue(value, "My Key", key);
	}

	private final List<IChartKey> workingKeys;

	private final IDataProvider provider;

	private final ChartFilter filter;

}