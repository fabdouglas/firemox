/*
 * Created on 5 févr. 2005
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

import java.util.ArrayList;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public class IntegerList extends ArrayList<Integer> {

	/**
	 * Create a new instance of this class.
	 */
	public IntegerList() {
		super();
	}

	/**
	 * Returns a shallow copy of this <tt>ArrayList</tt> instance. (The elements
	 * themselves are not copied.)
	 * 
	 * @return a clone of this <tt>ArrayList</tt> instance.
	 */
	@Override
	public IntegerList clone() {
		IntegerList v = (IntegerList) super.clone();
		return v;
	}

	/**
	 * Return the last integer of the list.
	 * 
	 * @return the last integer of the list.
	 */
	public int getLastInt() {
		return super.get(size() - 1).intValue();
	}

	/**
	 * Return the first integer of the list.
	 * 
	 * @return the first integer of the list.
	 */
	public int getFirstInt() {
		return super.get(0).intValue();
	}

	/**
	 * Returns the element at the specified position in this list.
	 * 
	 * @param index
	 *          index of element to return.
	 * @return the element at the specified position in this list.
	 */
	public int getInt(int index) {
		return super.get(index).intValue();
	}

	/**
	 * Appends the specified element to the end of this list.
	 * 
	 * @param value
	 *          element to be appended to this list.
	 */
	public void addInt(int value) {
		super.add(value);
	}

	/**
	 * Inserts the specified element at the specified position in this list.
	 * Shifts the element currently at that position (if any) and any subsequent
	 * elements to the right (adds one to their indices).
	 * 
	 * @param index
	 *          index at which the specified element is to be inserted.
	 * @param value
	 *          element to be inserted.
	 */
	public void addInt(int index, int value) {
		super.add(index, value);
	}

	/**
	 * Removes the element at the last position in this list. Shifts any
	 * subsequent elements to the left (subtracts one from their indices).
	 * 
	 * @return the element that was removed from the list.
	 */
	public int removeLastInt() {
		return super.remove(size() - 1).intValue();
	}

	/**
	 * Removes the element at the first position in this list. Shifts any
	 * subsequent elements to the left (subtracts one from their indices).
	 * 
	 * @return the element that was removed from the list.
	 */
	public int removeFirstInt() {
		return remove(0).intValue();
	}

	/**
	 * Removes the element at the specified position in this list. Shifts any
	 * subsequent elements to the left (subtracts one from their indices).
	 * 
	 * @param index
	 *          the index of the element to removed.
	 * @return the element that was removed from the list.
	 */
	public int removeInt(int index) {
		return super.remove(index).intValue();
	}
}
