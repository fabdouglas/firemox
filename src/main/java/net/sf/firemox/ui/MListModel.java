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
package net.sf.firemox.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;

import net.sf.firemox.tools.MCardCompare;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @param <T>
 *          the model type.
 * @since 0.94
 */
public class MListModel<T extends MCardCompare> extends AbstractListModel {

	/**
	 * The content.
	 */
	public final Vector<T> delegate = new Vector<T>();

	/**
	 * The original content.
	 */
	public final List<T> removedDelegate = new ArrayList<T>();

	/**
	 * The label reflecting the amount of this list.
	 */
	private final JLabel linkedAmount;

	/**
	 * If <code>true</code> the amount label is updated with the internal
	 * amount.
	 */
	private final boolean deepSum;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param linkedAmount
	 *          the label reflecting the amount of this list.
	 * @param deepSum
	 *          if <code>true</code> the amount label is updated with the
	 *          internal amount.
	 */
	public MListModel(JLabel linkedAmount, boolean deepSum) {
		super();
		this.linkedAmount = linkedAmount;
		this.deepSum = deepSum;
		updateAmount();
	}

	/**
	 * Returns the number of components in this list.
	 * <p>
	 * This method is identical to <code>size</code>, which implements the
	 * <code>List</code> interface defined in the 1.2 Collections framework.
	 * This method exists in conjunction with <code>setSize</code> so that
	 * <code>size</code> is identifiable as a JavaBean property.
	 * 
	 * @return the number of components in this list
	 * @see #size()
	 */
	public int getSize() {
		return delegate.size();
	}

	/**
	 * Returns the component at the specified index. <blockquote> <b>Note:</b>
	 * Although this method is not deprecated, the preferred method to use is
	 * <code>get(int)</code>, which implements the <code>List</code>
	 * interface defined in the 1.2 Collections framework. </blockquote>
	 * 
	 * @param index
	 *          an index into this list
	 * @return the component at the specified index
	 * @exception ArrayIndexOutOfBoundsException
	 *              if the <code>index</code> is negative or greater than the
	 *              current size of this list
	 */
	public T getElementAt(int index) {
		return delegate.get(index);
	}

	/**
	 * Returns the number of components in this list.
	 * 
	 * @return the number of components in this list
	 * @see List#size()
	 */
	public int size() {
		return delegate.size();
	}

	/**
	 * Tests whether this list has any components.
	 * 
	 * @return <code>true</code> if and only if this list has no components,
	 *         that is, its size is zero; <code>false</code> otherwise
	 * @see List#isEmpty()
	 */
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/**
	 * Returns an enumeration of the components of this list.
	 * 
	 * @return an enumeration of the components of this list
	 * @see List#iterator()
	 */
	public Iterator<T> iterator() {
		return delegate.iterator();
	}

	/**
	 * Searches for the first occurrence of <code>element</code>.
	 * 
	 * @param element
	 *          an object
	 * @return the index of the first occurrence of the argument in this list;
	 *         returns <code>-1</code> if the object is not found
	 * @see List#indexOf(Object)
	 */
	public int indexOf(T element) {
		return delegate.indexOf(element);
	}

	/**
	 * Searches for the first occurrence of <code>element</code>.
	 * 
	 * @param element
	 *          an object
	 * @return the index of the first occurrence of the argument in this list;
	 *         returns <code>-1</code> if the object is not found
	 * @see List#indexOf(Object)
	 */
	public int indexOf(String element) {
		for (int i = 0; i < delegate.size(); i++) {
			if (delegate.get(i).equals(element))
				return i;
		}
		return -1;
	}

	/**
	 * Adds the specified component to the end of this list.
	 * 
	 * @param obj
	 *          the component to be added
	 * @see List#add(Object)
	 */
	public void add(T obj) {
		int index = delegate.size();
		delegate.add(obj);
		fireIntervalAdded(this, index, index);
		updateAmount();
	}

	/**
	 * Removes the first (lowest-indexed) occurrence of the argument from this
	 * list.
	 * 
	 * @param obj
	 *          the component to be removed
	 * @return <code>true</code> if the argument was a component of this list;
	 *         <code>false</code> otherwise
	 * @see List#remove(Object)
	 */
	public boolean remove(T obj) {
		int index = indexOf(obj);
		boolean rv = delegate.remove(obj);
		if (index >= 0) {
			fireIntervalRemoved(this, index, index);
		}
		updateAmount();
		return rv;
	}

	/**
	 * Returns a string that displays and identifies this object's properties.
	 * 
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return delegate.toString();
	}

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element.
	 * <p>
	 * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out
	 * of range (<code>index &lt; 0 || index &gt;= size()</code>).
	 * 
	 * @param index
	 *          index of element to replace
	 * @param element
	 *          element to be stored at the specified position
	 * @return the element previously at the specified position
	 */
	public T set(int index, T element) {
		T rv = delegate.get(index);
		delegate.set(index, element);
		fireContentsChanged(this, index, index);
		updateAmount();
		return rv;
	}

	/**
	 * Inserts the specified object as a component in this list at the specified
	 * <code>index</code>.
	 * <p>
	 * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is
	 * invalid. <blockquote> <b>Note:</b> Although this method is not deprecated,
	 * the preferred method to use is <code>add(int,Object)</code>, which
	 * implements the <code>List</code> interface defined in the 1.2 Collections
	 * framework. </blockquote>
	 * 
	 * @param obj
	 *          the component to insert
	 * @param index
	 *          where to insert the new component
	 * @exception ArrayIndexOutOfBoundsException
	 *              if the index was invalid
	 */
	public void insertElementAt(T obj, int index) {
		delegate.insertElementAt(obj, index);
		fireIntervalAdded(this, index, index);
		updateAmount();
	}

	/**
	 * Removes the element at the specified position in this list. Returns the
	 * element that was removed from the list.
	 * <p>
	 * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out
	 * of range (<code>index &lt; 0 || index &gt;= size()</code>).
	 * 
	 * @param index
	 *          the index of the element to removed
	 * @return the removed object.
	 */
	public T remove(int index) {
		T rv = delegate.get(index);
		delegate.remove(index);
		fireIntervalRemoved(this, index, index);
		updateAmount();
		return rv;
	}

	/**
	 * Removes all of the elements from this list. The list will be empty after
	 * this call returns (unless it throws an exception).
	 */
	public void clear() {
		int index1 = delegate.size() - 1;
		delegate.clear();
		if (index1 >= 0) {
			fireIntervalRemoved(this, 0, index1);
		}
		updateAmount();
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence (from first to last element).
	 * <p>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this list. (In other words, this method must allocate a new
	 * array even if this list is backed by an array). The caller is thus free to
	 * modify the returned array.
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 * 
	 * @return an array containing all of the elements in this list in proper
	 *         sequence
	 * @see java.util.Arrays#asList(Object[])
	 */
	public Object[] toArray() {
		return delegate.toArray();
	}

	/**
	 * Removes from this Vector all of its elements that are contained in the
	 * specified Collection.
	 * 
	 * @param c
	 *          a collection of elements to be removed from the Vector
	 * @return true if this Vector changed as a result of the call
	 * @throws ClassCastException
	 *           if the types of one or more elements in this vector are
	 *           incompatible with the specified collection (optional)
	 * @throws NullPointerException
	 *           if this vector contains one or more null elements and the
	 *           specified collection does not support null elements (optional),
	 *           or if the specified collection is null
	 * @since 1.2
	 */
	public boolean removeAll(Collection<T> c) {
		removedDelegate.addAll(c);
		delegate.removeAll(c);
		updateAmount();
		fireIntervalRemoved(this, 0, 0);
		return true;
	}

	/**
	 * Appends all of the elements in the specified Collection to the end of this
	 * Vector, in the order that they are returned by the specified Collection's
	 * Iterator. The behavior of this operation is undefined if the specified
	 * Collection is modified while the operation is in progress. (This implies
	 * that the behavior of this call is undefined if the specified Collection is
	 * this Vector, and this Vector is nonempty.)
	 * 
	 * @param c
	 *          elements to be inserted into this Vector
	 * @return {@code true} if this Vector changed as a result of the call
	 * @throws NullPointerException
	 *           if the specified collection is null
	 * @since 1.2
	 */
	public boolean addAll(Collection<T> c) {
		delegate.addAll(c);
		removedDelegate.removeAll(c);
		Collections.sort(delegate);
		refresh();
		return true;
	}

	/**
	 * Refresh the list.
	 */
	public void refresh() {
		updateAmount();
		fireIntervalAdded(this, 0, 0);
	}

	/**
	 * Update the amount in the attached label.
	 */
	private void updateAmount() {
		int rightAmount;
		if (deepSum) {
			rightAmount = 0;
			for (MCardCompare card : delegate) {
				rightAmount += card.getAmount();
			}
		} else {
			rightAmount = removedDelegate.size() + delegate.size();
		}
		linkedAmount.setText("<html>" + delegate.size() + "/" + rightAmount);
	}
}
