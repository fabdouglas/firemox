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
package net.sf.firemox.tools;

import java.util.AbstractList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.RandomAccess;

/**
 * Resizable-array implementation of the <tt>List</tt> interface. Implements
 * all optional list operations, and permits all elements, including
 * <tt>null</tt>. In addition to implementing the <tt>List</tt> interface,
 * this class provides methods to manipulate the size of the array that is used
 * internally to store the list. (This class is roughly equivalent to
 * <tt>Vector</tt>, except that it is unsynchronized.)
 * <p>
 * The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, <tt>set</tt>,
 * <tt>iterator</tt>, and <tt>listIterator</tt> operations run in constant
 * time. The <tt>add</tt> operation runs in <i>amortized constant time</i>,
 * that is, adding n elements requires O(n) time. All of the other operations
 * run in linear time (roughly speaking). The constant factor is low compared to
 * that for the <tt>LinkedList</tt> implementation.
 * <p>
 * Each <tt>ArrayList</tt> instance has a <i>capacity</i>. The capacity is
 * the size of the array used to store the elements in the list. It is always at
 * least as large as the list size. As elements are added to an ArrayList, its
 * capacity grows automatically. The details of the growth policy are not
 * specified beyond the fact that adding an element has constant amortized time
 * cost.
 * <p>
 * An application can increase the capacity of an <tt>ArrayList</tt> instance
 * before adding a large number of elements using the <tt>ensureCapacity</tt>
 * operation. This may reduce the amount of incremental reallocation.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access an <tt>ArrayList</tt> instance concurrently, and at
 * least one of the threads modifies the list structurally, it <i>must</i> be
 * synchronized externally. (A structural modification is any operation that
 * adds or deletes one or more elements, or explicitly resizes the backing
 * array; merely setting the value of an element is not a structural
 * modification.) This is typically accomplished by synchronizing on some object
 * that naturally encapsulates the list. If no such object exists, the list
 * should be "wrapped" using the
 * {@link java.util.Collections#synchronizedList(List) Collections.synchronizedList}
 * method. This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:
 * 
 * <pre>
 *    List list = Collections.synchronizedList(new ArrayList(...));
 * </pre>
 * 
 * <p>
 * The iterators returned by this class's <tt>iterator</tt> and
 * <tt>listIterator</tt> methods are <i>fail-fast</i>: if the list is
 * structurally modified at any time after the iterator is created, in any way
 * except through the iterator's own <tt>remove</tt> or <tt>add</tt>
 * methods, the iterator will throw a {@link ConcurrentModificationException}.
 * Thus, in the face of concurrent modification, the iterator fails quickly and
 * cleanly, rather than risking arbitrary, non-deterministic behavior at an
 * undetermined time in the future.
 * <p>
 * Note that the fail-fast behavior of an iterator cannot be guaranteed as it
 * is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification. Fail-fast iterators throw
 * <tt>ConcurrentModificationException</tt> on a best-effort basis. Therefore,
 * it would be wrong to write a program that depended on this exception for its
 * correctness: <i>the fail-fast behavior of iterators should be used only to
 * detect bugs.</i>
 * <p>
 * This class is a member of the <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 * 
 * @see Collection
 * @see List
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @param <E>
 * @since 0.91
 */
public class RevertedArrayList<E> extends AbstractList<E> implements List<E>,
		RandomAccess, Cloneable, java.io.Serializable {

	/**
	 * The array buffer into which the elements of the ArrayList are stored. The
	 * capacity of the ArrayList is the length of this array buffer.
	 */
	private transient E[] elementData;

	/**
	 * The size of the ArrayList (the number of elements it contains).
	 * 
	 * @serial
	 */
	private int size;

	/**
	 * Constructs an empty list with the specified initial capacity.
	 * 
	 * @param initialCapacity
	 *          the initial capacity of the list
	 */
	@SuppressWarnings("unchecked")
	public RevertedArrayList(int initialCapacity) {
		super();
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		this.elementData = (E[]) new Object[initialCapacity];
	}

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public RevertedArrayList() {
		this(10);
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in
	 * the order they are returned by the collection's iterator. The
	 * <tt>ArrayList</tt> instance has an initial capacity of 110% the size of
	 * the specified collection.
	 * 
	 * @param c
	 *          the collection whose elements are to be placed into this list
	 */
	@SuppressWarnings("unchecked")
	public RevertedArrayList(Collection<? extends E> c) {
		size = c.size();
		// Allow 10% room for growth
		int capacity = (int) Math.min((size * 110L) / 100, Integer.MAX_VALUE);
		elementData = (E[]) c.toArray(new Object[capacity]);
	}

	/**
	 * Increases the capacity of this <tt>ArrayList</tt> instance, if necessary,
	 * to ensure that it can hold at least the number of elements specified by the
	 * minimum capacity argument.
	 * 
	 * @param minCapacity
	 *          the desired minimum capacity
	 */
	@SuppressWarnings("unchecked")
	public void ensureCapacity(int minCapacity) {
		modCount++;
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
			Object[] oldData = elementData;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			elementData = (E[]) new Object[newCapacity];
			System.arraycopy(oldData, 0, elementData, 0, size);
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	public int indexOf(Object elem) {
		if (elem == null) {
			for (int i = 0; i < size; i++)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = 0; i < size; i++)
				if (elem.equals(elementData[i]))
					return i;
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object elem) {
		if (elem == null) {
			for (int i = size - 1; i >= 0; i--)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = size - 1; i >= 0; i--)
				if (elem.equals(elementData[i]))
					return i;
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RevertedArrayList<E> clone() {
		try {
			RevertedArrayList<E> v = (RevertedArrayList<E>) super.clone();
			v.elementData = (E[]) new Object[size];
			System.arraycopy(elementData, 0, v.elementData, 0, size);
			v.modCount = 0;
			return v;
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[size];
		System.arraycopy(elementData, 0, result, 0, size);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		T[] aTmp = a;
		if (a.length < size)
			aTmp = (T[]) java.lang.reflect.Array.newInstance(a.getClass()
					.getComponentType(), size);
		System.arraycopy(elementData, 0, aTmp, 0, size);
		if (aTmp.length > size)
			aTmp[size] = null;
		return aTmp;
	}

	// Positional Access Operations

	@Override
	public E get(int index) {
		rangeCheck(index);

		return elementData[size - index - 1];
	}

	@Override
	public E set(int index, E element) {
		rangeCheck(index);

		E oldValue = elementData[index];
		elementData[index] = element;
		return oldValue;
	}

	@Override
	public boolean add(E e) {
		ensureCapacity(size + 1); // Increments modCount!!
		elementData[size++] = e;
		return true;
	}

	@Override
	public void add(int index, E element) {
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

		ensureCapacity(size + 1); // Increments modCount!!
		System.arraycopy(elementData, index, elementData, index + 1, size - index);
		elementData[index] = element;
		size++;
	}

	@Override
	public E remove(int index) {
		rangeCheck(index);

		modCount++;
		E oldValue = elementData[index];

		int numMoved = size - index - 1;
		if (numMoved > 0)
			System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		elementData[--size] = null; // Let gc do its work

		return oldValue;
	}

	@Override
	public boolean remove(Object o) {
		if (o == null) {
			for (int index = 0; index < size; index++)
				if (elementData[index] == null) {
					fastRemove(index);
					return true;
				}
		} else {
			for (int index = 0; index < size; index++)
				if (o.equals(elementData[index])) {
					fastRemove(index);
					return true;
				}
		}
		return false;
	}

	/*
	 * Private remove method that skips bounds checking and does not return the
	 * value removed.
	 */
	private void fastRemove(int index) {
		modCount++;
		int numMoved = size - index - 1;
		if (numMoved > 0)
			System.arraycopy(elementData, index + 1, elementData, index, numMoved);
		elementData[--size] = null; // Let gc do its work
	}

	@Override
	public void clear() {
		modCount++;

		// Let gc do its work
		for (int i = 0; i < size; i++)
			elementData[i] = null;

		size = 0;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacity(size + numNew); // Increments modCount
		System.arraycopy(a, 0, elementData, size, numNew);
		size += numNew;
		return numNew != 0;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		if (index > size || index < 0)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

		Object[] a = c.toArray();
		int numNew = a.length;
		ensureCapacity(size + numNew); // Increments modCount

		int numMoved = size - index;
		if (numMoved > 0)
			System.arraycopy(elementData, index, elementData, index + numNew,
					numMoved);

		System.arraycopy(a, 0, elementData, index, numNew);
		size += numNew;
		return numNew != 0;
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		modCount++;
		int numMoved = size - toIndex;
		System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

		// Let gc do its work
		int newSize = size - (toIndex - fromIndex);
		while (size != newSize)
			elementData[--size] = null;
	}

	/**
	 * Checks if the given index is in range. If not, throws an appropriate
	 * runtime exception. This method does *not* check if the index is negative:
	 * It is always used immediately prior to an array access, which throws an
	 * ArrayIndexOutOfBoundsException if index is negative.
	 */
	private void rangeCheck(int index) {
		if (index >= size)
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
	}

	/**
	 * Save the state of the <tt>ArrayList</tt> instance to a stream (that is,
	 * serialize it).
	 * 
	 * @serialData The length of the array backing the <tt>ArrayList</tt>
	 *             instance is emitted (int), followed by all of its elements
	 *             (each an <tt>Object</tt>) in the proper order.
	 */
	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {
		// Write out element count, and any hidden stuff
		int expectedModCount = modCount;
		s.defaultWriteObject();

		// Write out array length
		s.writeInt(elementData.length);

		// Write out all elements in the proper order.
		for (int i = 0; i < size; i++)
			s.writeObject(elementData[i]);

		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}

	}

	/**
	 * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
	 * deserialize it).
	 */
	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		// Read in size, and any hidden stuff
		s.defaultReadObject();

		// Read in array length and allocate array
		int arrayLength = s.readInt();
		elementData = (E[]) new Object[arrayLength];
		Object[] a = elementData;

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++)
			a[i] = s.readObject();
	}
}
