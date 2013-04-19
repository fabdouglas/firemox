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
package net.sf.firemox.action.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @param <T>
 *          the object type encapsulated in this context.
 * @since 0.86
 */
public class ObjectArray<T> implements ActionContext, Iterable<T> {

	/**
	 * Create a new context with a new object array
	 * 
	 * @param size
	 *          the array's size of this context.
	 */
	public ObjectArray(int size) {
		this.array = new ArrayList<T>(size);
		for (int i = size; i-- > 0;) {
			array.add(null);
		}
	}

	/**
	 * Return a object value of this context.
	 * 
	 * @param index
	 *          the object index to be returned
	 * @return the object value of this context.
	 */
	public T getObject(int index) {
		return array.get(index);
	}

	/**
	 * Set an object value of this context.
	 * 
	 * @param index
	 *          the object index to be set
	 * @param b
	 *          the object to set
	 */
	public void setObject(int index, T b) {
		array.set(index, b);
	}

	/**
	 * Return amount of objects.
	 * 
	 * @return amount of objects.
	 */
	public int size() {
		return array.size();
	}

	/**
	 * The object values of this context.
	 */
	private List<T> array;

	public Iterator<T> iterator() {
		return array.iterator();
	}
}
