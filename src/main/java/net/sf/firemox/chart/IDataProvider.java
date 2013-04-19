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

import java.util.Collection;
import java.util.List;

import net.sf.firemox.clickable.target.card.CardModel;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public interface IDataProvider {

	/**
	 * Return the keys corresponding to the given card.
	 * 
	 * @param cardModel
	 *          the requested key.
	 * @param filter
	 *          the filter to apply to retrieve the keys.
	 * @return the keys corresponding to the given card.
	 */
	Collection<IChartKey> getKeys(CardModel cardModel, ChartFilter filter);

	/**
	 * Return working set keys
	 * 
	 * @param filter
	 *          the filter to apply to retrieve the valid keys.
	 * @return working set keys
	 */
	List<IChartKey> getKeys(ChartFilter filter);
}
