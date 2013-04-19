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
package net.sf.firemox.tools;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @param <K>
 *          key
 * @param <V>
 *          value
 * @since 0.80
 */
public class Pair<K extends Comparable<K>, V> implements Comparable<Pair<K, V>> {

	/**
	 * The stored string instance. (key)
	 */
	public K key;

	/**
	 * The stored string instance. (value)
	 */
	public V value;

	/**
	 * Creates a new instance of this class <br>
	 * 
	 * @param key
	 * @param value
	 */
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public int compareTo(Pair<K, V> arg0) {
		return key.compareTo(arg0.key);
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof String) {
			return key.toString().equals(arg0);
		} else if (arg0 instanceof Pair) {
			Pair<?, ?> pair = (Pair<?, ?>) arg0;
			return key.equals(pair.key);
		}
		return false;
	}

	@Override
	public String toString() {
		return "{" + key + ":" + value + "}";
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
