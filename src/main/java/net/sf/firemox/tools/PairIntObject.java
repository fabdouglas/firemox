/*
 * Created on Dec 6, 2004 
 * Original filename was PairIntObject.java
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
 * 
 */
package net.sf.firemox.tools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @param <T>
 *          The type of value.
 * @since 0.81
 */
public class PairIntObject<T> implements Comparable<PairIntObject<T>> {

	/**
	 * The stored int value.
	 */
	public int key;

	/**
	 * The stored object.
	 */
	public T value;

	/**
	 * Creates a new instance of this class <br>
	 * 
	 * @param key
	 *          the key
	 * @param value
	 *          the value
	 */
	public PairIntObject(int key, T value) {
		this.key = key;
		this.value = value;
	}

	public int compareTo(PairIntObject<T> arg0) {
		return key - arg0.key;
	}

	@Override
	public boolean equals(Object context) {
		return context == this;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return "{" + key + ":" + value + "}";
	}

}
