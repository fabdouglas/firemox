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
package net.sf.firemox.expression.intlist;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.92
 */
public enum ListType {

	/**
	 * This list is only defined by two attributes : start and end included
	 * limits.
	 */
	RANGE,

	/**
	 * This list is defined by a set of values.
	 */
	COLLECTION;

	/**
	 * Build a new list from the given values following the type list.
	 * 
	 * @param result
	 *          the input result.
	 * @return the interpreted list.
	 */
	public int[] getList(int[] result) {
		switch (this) {
		case COLLECTION:
		default:
			return result;
		case RANGE:
			final int[] rangeResult = new int[result[1] - result[0] + 1];
			int start = result[0];
			for (int i = 0; i < rangeResult.length; i++) {
				rangeResult[i] = start + i;
			}
			return rangeResult;
		}
	}
}
